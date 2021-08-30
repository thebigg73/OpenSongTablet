package com.garethevans.church.opensongtablet.performance;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class DisplayPrevNext {
    // This deals with showing the previous and next song buttons and their action
    private final MainActivityInterface mainActivityInterface;
    private final LinearLayout layout;
    private final ExtendedFloatingActionButton prev, next;
    private boolean showPrev, prevVisible = false;
    private boolean showNext, nextVisible = false;
    private boolean prevNextSongMenu;
    private final String TAG = "DisplayPrevNext";
    private final Runnable hideNextRunnable = new Runnable() {
        @Override
        public void run() {
            next.hide();
            nextVisible = false;
        }
    };
    private final Runnable hidePrevRunnable = new Runnable() {
        @Override
        public void run() {
            prev.hide();
            prevVisible = false;
        }
    };

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
        prevNextSongMenu = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "prevNextSongMenu", false);
        if (showPrev) {
            layout.findViewById(R.id.prevHolder).setVisibility(View.VISIBLE);
        } else {
            layout.findViewById(R.id.prevHolder).setVisibility(View.GONE);
        }
        if (showNext) {
            layout.findViewById(R.id.nextHolder).setVisibility(View.VISIBLE);
        } else {
            layout.findViewById(R.id.nextHolder).setVisibility(View.GONE);
        }
    }

    public boolean getShowPrev() {
        return showPrev;
    }
    public boolean getShowNext() {
        return showNext;
    }

    public void setPrevNext() {
        next.hide();
        prev.hide();
        nextVisible = false;
        prevVisible = false;
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
                    if (key!=null && !key.isEmpty() && !key.equals("null")) {
                        previousText = previousText + " (" + key + ")";
                    }
                    prev.setOnClickListener(v -> mainActivityInterface.loadSongFromSet(setPosition-1));
                }
                if (setPosition < mainActivityInterface.getCurrentSet().getSetItems().size() - 1 && showNext) {
                    nextText = mainActivityInterface.getCurrentSet().getFilename(setPosition + 1);
                    String key = mainActivityInterface.getCurrentSet().getKey(setPosition + 1);
                    if (key!=null && !key.isEmpty() && !key.equals("null")) {
                        nextText = nextText + " (" + key + ")";
                    }
                    next.setOnClickListener(v -> mainActivityInterface.loadSongFromSet(setPosition+1));

                }
            } else if (prevNextSongMenu) {
                // Not in a set, so get the index in the song menu
                Log.d(TAG, "Not in a set");
                if (songPosition>0 && showPrev) {
                    previousText = mainActivityInterface.getSongInMenu(songPosition - 1).getTitle();
                    String key = mainActivityInterface.getSongInMenu(songPosition - 1).getKey();
                    if (key!=null && !key.isEmpty() && !key.equals("null")) {
                        previousText = previousText + " (" + key + ")";
                    }
                    prev.setOnClickListener(v -> mainActivityInterface.doSongLoad(mainActivityInterface.getSongInMenu(songPosition-1).getFolder(),
                            mainActivityInterface.getSongInMenu(songPosition-1).getFilename()));
                }
                if (songPosition<mainActivityInterface.getSongsInMenu().size()-1 && showNext) {
                    nextText = mainActivityInterface.getSongInMenu(songPosition + 1).getTitle();
                    String key = mainActivityInterface.getSongInMenu(songPosition + 1).getKey();
                    if (key!=null && !key.isEmpty() && !key.equals("null")) {
                        nextText = nextText + " (" + key + ")";
                    }
                    next.setOnClickListener(v -> mainActivityInterface.doSongLoad(mainActivityInterface.getSongInMenu(songPosition+1).getFolder(),
                            mainActivityInterface.getSongInMenu(songPosition+1).getFilename()));
                }
            }
            next.setText(nextText);
            prev.setText(previousText);
            showAndHide();
        }
    }

    public void showAndHide() {
        if (showNext && !next.getText().toString().isEmpty() && !nextVisible) {
            nextVisible = true;
            next.removeCallbacks(hideNextRunnable);
            next.show();
            next.postDelayed(hideNextRunnable, 5000);
        }
        if (showPrev && !prev.getText().toString().isEmpty() && !prevVisible) {
            prevVisible = true;
            prev.removeCallbacks(hidePrevRunnable);
            prev.show();
            prev.postDelayed(hidePrevRunnable, 5000);
        }
    }

    public void updateColors() {
        float buttonAlpha = mainActivityInterface.getMyThemeColors().getPageButtonsSplitAlpha();
        int buttonColor = mainActivityInterface.getMyThemeColors().getPageButtonsSplitColor();
        int buttonIconColor = mainActivityInterface.getMyThemeColors().getExtraInfoTextColor();
        prev.setIconTint(ColorStateList.valueOf(buttonIconColor));
        next.setIconTint(ColorStateList.valueOf(buttonIconColor));
        prev.setBackgroundTintList(ColorStateList.valueOf(buttonColor));
        next.setBackgroundTintList(ColorStateList.valueOf(buttonColor));
        layout.setAlpha(buttonAlpha);
    }

}
