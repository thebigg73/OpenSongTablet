package com.garethevans.church.opensongtablet.performance;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class DisplayPrevNext {
    // This deals with showing the previous and next song buttons and their action
    private final MainActivityInterface mainActivityInterface;
    private float buttonAlpha;
    private int buttonColor;
    private int buttonIconColor;
    private final LinearLayout layout;
    private final ExtendedFloatingActionButton prev, next;
    private boolean showPrev;
    private boolean showNext;
    private final String TAG = "DisplayPrevNext";

    public DisplayPrevNext(Context c, MainActivityInterface mainActivityInterface, LinearLayout layout,
                           ExtendedFloatingActionButton prev, ExtendedFloatingActionButton next) {
        this.mainActivityInterface = mainActivityInterface;
        this.layout = layout;
        this.prev = prev;
        this.next = next;
        updateShow(c, mainActivityInterface);
        updateColors();
    }

    public void updateShow(Context c, MainActivityInterface mainActivityInterface) {
        showPrev = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"prevInSet", false);
        showNext = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"nextInSet", true);
    }

    public boolean getShowPrev() {
        return showPrev;
    }
    public boolean getShowNext() {
        return showNext;
    }

    public void setPrevNext() {
        if (showNext || showPrev) {
            int setPosition = mainActivityInterface.getCurrentSet().getIndexSongInSet();
            int songPosition = mainActivityInterface.getPositionOfSongInMenu();
            String previousText = "";
            String nextText = "";
            if (setPosition > -1) {
                // We are in a set
                if (setPosition > 0 && showPrev) {
                    previousText = mainActivityInterface.getCurrentSet().getFilename(setPosition - 1);
                    String key = mainActivityInterface.getCurrentSet().getKey(setPosition - 1);
                    if (!key.isEmpty() && !key.equals("null")) {
                        previousText = previousText + " (" + key + ")";
                    }
                    prev.setOnClickListener(v -> mainActivityInterface.loadSongFromSet(setPosition-1));
                }
                if (setPosition < mainActivityInterface.getCurrentSet().getSetItems().size() - 1 && showNext) {
                    nextText = mainActivityInterface.getCurrentSet().getFilename(setPosition + 1);
                    String key = mainActivityInterface.getCurrentSet().getKey(setPosition + 1);
                    if (!key.isEmpty() && !key.equals("null")) {
                        nextText = nextText + " (" + key + ")";
                    }
                    next.setOnClickListener(v -> mainActivityInterface.loadSongFromSet(setPosition+1));

                }
            } else {
                // Not in a set, so get the index in the song menu
                Log.d(TAG, "Not in a set");
                if (songPosition>0 && showPrev) {
                    previousText = mainActivityInterface.getSongInMenu(songPosition - 1).getTitle();
                    String key = mainActivityInterface.getSongInMenu(songPosition - 1).getKey();
                    if (!key.isEmpty() && !key.equals("null")) {
                        previousText = previousText + " (" + key + ")";
                    }
                    prev.setOnClickListener(v -> mainActivityInterface.doSongLoad(mainActivityInterface.getSongInMenu(songPosition-1).getFolder(),
                            mainActivityInterface.getSongInMenu(songPosition-1).getFilename()));
                }
                if (songPosition<mainActivityInterface.getSongsInMenu().size()-1 && showNext) {
                    nextText = mainActivityInterface.getSongInMenu(songPosition + 1).getTitle();
                    String key = mainActivityInterface.getSongInMenu(songPosition + 1).getKey();
                    if (!key.isEmpty() && !key.equals("null")) {
                        nextText = nextText + " (" + key + ")";
                    }
                    next.setOnClickListener(v -> mainActivityInterface.doSongLoad(mainActivityInterface.getSongInMenu(songPosition+1).getFolder(),
                            mainActivityInterface.getSongInMenu(songPosition+1).getFilename()));
                }
            }
            Log.d(TAG, "setSize="+mainActivityInterface.getCurrentSet().getSetItems().size());
            Log.d(TAG, "songListSize="+mainActivityInterface.getSongsInMenu().size());
            Log.d(TAG, "nextText="+nextText);
            Log.d(TAG, "prevText="+previousText);
            Log.d(TAG, "songPosition="+songPosition);
            Log.d(TAG, "setPosition="+setPosition);

            if (showNext && !nextText.isEmpty()) {
                next.setVisibility(View.VISIBLE);
            } else {
                next.setVisibility(View.GONE);
            }
            if (showPrev && !previousText.isEmpty()) {
                prev.setVisibility(View.VISIBLE);
            } else {
                prev.setVisibility(View.GONE);
            }
            layout.invalidate();
            next.setText(nextText);
            prev.setText(previousText);

        }
    }

    public void updateColors() {
        buttonAlpha = mainActivityInterface.getMyThemeColors().getPageButtonsSplitAlpha();
        buttonColor = mainActivityInterface.getMyThemeColors().getPageButtonsSplitColor();
        buttonIconColor = mainActivityInterface.getMyThemeColors().getExtraInfoTextColor();
        prev.setIconTint(ColorStateList.valueOf(buttonIconColor));
        next.setIconTint(ColorStateList.valueOf(buttonIconColor));
        prev.setBackgroundTintList(ColorStateList.valueOf(buttonColor));
        next.setBackgroundTintList(ColorStateList.valueOf(buttonColor));
        layout.setAlpha(buttonAlpha);
        if (showNext || showPrev) {
            layout.setVisibility(View.VISIBLE);
        } else {
            layout.setVisibility(View.GONE);
        }
    }

}
