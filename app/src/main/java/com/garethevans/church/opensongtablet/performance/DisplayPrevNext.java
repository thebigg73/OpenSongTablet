package com.garethevans.church.opensongtablet.performance;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.LinearLayout;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class DisplayPrevNext {
    // This deals with showing the previous and next song buttons and their actions
    // Even if we don't display the buttons, this sets up what to do on next/prev action (swipe, pedal, etc)

    private final MainActivityInterface mainActivityInterface;
    private final LinearLayout layout;
    private final ExtendedFloatingActionButton prev, next;
    private boolean showPrev, prevVisible = false;
    private boolean showNext, nextVisible = false;
    private boolean prevNextSongMenu;
    private boolean moveToSongInSet;
    private int prevIndex, nextIndex;
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
        int setPosition = mainActivityInterface.getCurrentSet().getIndexSongInSet();
        int songPosition = mainActivityInterface.getPositionOfSongInMenu();

        // Set the local variables for prevIndex, nextIndex and if we are using the set or song menu
        setIndexes(setPosition, songPosition);

        // If we are showing the buttons, deal with them
        if (showNext || showPrev) {
            // Get the text from either the set or song menu
            String previousText = getTextForButton(prevIndex);
            String nextText = getTextForButton(nextIndex);

            // Set the listeners
            prev.setOnClickListener(v -> doMove(prevIndex));
            next.setOnClickListener(v -> doMove(nextIndex));

            // Update the text
            next.setText(nextText);
            prev.setText(previousText);

            // This shows the ones chosen if not empty, then hides again
            showAndHide();


            /*if (moveToSongInSet) {
                // We are in a set
                if (prevIndex != -1 && showPrev) {
                    previousText = mainActivityInterface.getCurrentSet().getFilename(prevIndex);
                    String key = mainActivityInterface.getCurrentSet().getKey(prevIndex);
                    if (key!=null && !key.isEmpty() && !key.equals("null")) {
                        previousText = previousText + " (" + key + ")";
                    }
                    prev.setOnClickListener(v -> mainActivityInterface.loadSongFromSet(prevIndex));
                }
                if (nextIndex != -1 && showNext) {
                    nextText = mainActivityInterface.getCurrentSet().getFilename(nextIndex);
                    String key = mainActivityInterface.getCurrentSet().getKey(nextIndex);
                    if (key!=null && !key.isEmpty() && !key.equals("null")) {
                        nextText = nextText + " (" + key + ")";
                    }
                    next.setOnClickListener(v -> mainActivityInterface.loadSongFromSet(nextIndex));

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
            }*/

        }
    }

    private void setIndexes(int setPosition, int songPosition) {
        if (setPosition>=0) {
            moveToSongInSet = true;
            if (setPosition>0) {
                prevIndex = setPosition - 1;
            } else {
                prevIndex = -1;
            }
            if (setPosition < mainActivityInterface.getCurrentSet().getSetItems().size() - 1) {
                nextIndex = setPosition + 1;
            } else {
                nextIndex = -1;
            }
        } else {
            moveToSongInSet = false;
            if (songPosition>0) {
                prevIndex = songPosition - 1;
            } else {
                prevIndex = -1;
            }
            if (songPosition < mainActivityInterface.getSongsInMenu().size()-1) {
                nextIndex = songPosition + 1;
            } else {
                nextIndex = -1;
            }
        }
    }

    private String getTextForButton(int position) {
        String text = "";
        if (position>-1) {
            if (moveToSongInSet) {
                if (position < mainActivityInterface.getCurrentSet().getSetItems().size()) {
                    text = mainActivityInterface.getCurrentSet().getFilename(nextIndex);
                    String key = mainActivityInterface.getCurrentSet().getKey(nextIndex);
                    if (key != null && !key.isEmpty() && !key.equals("null")) {
                        text = text + " (" + key + ")";
                    }
                }
            } else {
                if (position < mainActivityInterface.getSongsInMenu().size()) {
                    text = mainActivityInterface.getSongInMenu(position).getTitle();
                    String key = mainActivityInterface.getSongInMenu(position).getKey();
                    if (key != null && !key.isEmpty() && !key.equals("null")) {
                        text = text + " (" + key + ")";
                    }
                }
            }
        }
        return text;
    }

    public void moveToNext() {
        doMove(nextIndex);
    }
    public void moveToPrev() {
        doMove(prevIndex);
    }

    private void doMove(int position) {
        if (moveToSongInSet) {
            mainActivityInterface.loadSongFromSet(position);
        } else {
            mainActivityInterface.doSongLoad(mainActivityInterface.getSongInMenu(position).getFolder(),
                    mainActivityInterface.getSongInMenu(position).getFilename());
        }
    }

    public void showAndHide() {
        // If in using song menu, don't proceed unless user has switched on this preference
        // If in a set, only show if showNext/showPrev is selected
        if (moveToSongInSet || prevNextSongMenu) {
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
