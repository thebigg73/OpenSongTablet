package com.garethevans.church.opensongtablet.performance;

import android.content.Context;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class DisplayPrevNext {
    // This deals with showing the previous and next song buttons and their actions
    // Even if we don't display the buttons, this sets up what to do on next/prev action (swipe, pedal, etc)

    private final MainActivityInterface mainActivityInterface;
    private final Context c;
    private final String TAG = "DisplayPrevNext";
    private final LinearLayout layout;
    private final ExtendedFloatingActionButton prev, next;
    private boolean showPrev, prevVisible = false;
    private boolean showNext, nextVisible = false;
    private boolean prevNextSongMenu;
    private boolean movePrevInSet, moveNextInSet, moveNextInMenu, movePrevInMenu;
    private String swipeDirection = "R2L";
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

    public DisplayPrevNext (Context c, LinearLayout layout,
                           ExtendedFloatingActionButton prev, ExtendedFloatingActionButton next) {
        this.c = c;
        this.mainActivityInterface = (MainActivityInterface) c;
        this.layout = layout;
        this.prev = prev;
        this.next = next;
        updateShow();
        updateColors();
    }

    public void updateShow() {
        showPrev = mainActivityInterface.getPreferences().getMyPreferenceBoolean("prevInSet", false);
        showNext = mainActivityInterface.getPreferences().getMyPreferenceBoolean("nextInSet", true);
        prevNextSongMenu = mainActivityInterface.getPreferences().getMyPreferenceBoolean("prevNextSongMenu", false);
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

        getPositions();
        // If we are showing the buttons, deal with them
        if (showNext || showPrev) {
            // Get the text from either the set or song menu
            // Decode the text which for non-songs may be Uri encoded for safety
            String previousText = Uri.decode(getTextForButton(prevIndex));
            String nextText = Uri.decode(getTextForButton(nextIndex));

            // Set the listeners
            // Use the text as it is for the filename (might be Uri encoded)
            prev.setOnClickListener(v -> moveToPrev());
            next.setOnClickListener(v -> moveToNext());

            // Update the text
            next.setText(nextText);
            prev.setText(previousText);

            // This shows the ones chosen if not empty, then hides again
            showAndHide();
        }
    }

    private void getPositions() {
        int setPosition = mainActivityInterface.getCurrentSet().getIndexSongInSet();
        int songPosition = mainActivityInterface.getPositionOfSongInMenu();

        Log.d(TAG,"songPosition="+songPosition+"  setPosition="+setPosition);
        // Set the local variables for prevIndex, nextIndex and if we are using the set or song menu
        setIndexes(setPosition, songPosition);
    }

    private void setIndexes(int setPosition, int songPosition) {
        if (setPosition>=0) {
            moveNextInMenu = false;
            movePrevInMenu = false;
            if (setPosition>0) {
                prevIndex = setPosition - 1;
                movePrevInSet = true;
            } else {
                prevIndex = -1;
                movePrevInSet = false;
            }
            if (setPosition < mainActivityInterface.getCurrentSet().getSetItems().size() - 1) {
                nextIndex = setPosition + 1;
                moveNextInSet = true;
            } else {
                nextIndex = -1;
                moveNextInSet = false;
            }
        } else {
            moveNextInSet = false;
            movePrevInSet = false;
            if (songPosition>0) {
                prevIndex = songPosition - 1;
                movePrevInMenu = true;
            } else {
                prevIndex = -1;
                movePrevInMenu = false;
            }
            if (songPosition < mainActivityInterface.getSongsInMenu().size()-1) {
                nextIndex = songPosition + 1;
                moveNextInMenu = true;
            } else {
                nextIndex = -1;
                moveNextInMenu = false;
            }
        }
        Log.d(TAG,"songPosition="+songPosition+"  movePrevInMenu="+movePrevInMenu+"  prevIndex="+prevIndex+"  moveNextInMenu="+moveNextInMenu+"  nextIndex="+nextIndex);
    }

    private String getTextForButton(int position) {
        Log.d(TAG,"getTextForButton("+position+")");
        String text = "";
        if (position>-1) {
            if (isSetMove(position)) {
                if (position < mainActivityInterface.getCurrentSet().getSetItems().size()) {
                    text = mainActivityInterface.getCurrentSet().getFilename(position);

                    Log.d(TAG,"next song in set="+mainActivityInterface.getCurrentSet().getFolder(position)+"/"+text);
                    // Look for the key in the set (it might be specified)
                    String key = mainActivityInterface.getCurrentSet().getKey(position);
                    // If it isn't there, for the song key from the user database instead
                    if (key==null || key.isEmpty()) {
                        key = mainActivityInterface.getSQLiteHelper().getKey(
                                mainActivityInterface.getCurrentSet().getFolder(position),
                                mainActivityInterface.getCurrentSet().getFilename(position));
                    }

                    if (key != null && !key.isEmpty() && !key.equals("null")) {
                        text = text + " (" + key + ")";
                    }
                }
            } else if (isMenuMove(position)){
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
        swipeDirection = "R2L";
        if (nextIndex!=-1) {
            doMove(nextIndex);
        } else {
            mainActivityInterface.getShowToast().doIt(c.getString(R.string.last_song));
        }
    }
    public void moveToPrev() {
        swipeDirection = "L2R";
        if (prevIndex != -1) {
            doMove(prevIndex);
        } else {
            mainActivityInterface.getShowToast().doIt(c.getString(R.string.first_song));
        }
    }

    private void doMove(int position) {
        Log.d(TAG,"position="+position);
        Log.d(TAG,"isSetMove="+isSetMove(position));
        Log.d(TAG,"isMenuMove="+isMenuMove(position));

        if (isSetMove(position)) {
            mainActivityInterface.loadSongFromSet(position);
        } else if (isMenuMove(position)){
            Log.d(TAG,"folder/filename"+mainActivityInterface.getSongInMenu(position).getFolder()+"/"+
                    mainActivityInterface.getSongInMenu(position).getFilename());
            mainActivityInterface.doSongLoad(mainActivityInterface.getSongInMenu(position).getFolder(),
                    mainActivityInterface.getSongInMenu(position).getFilename(),true);
        }
    }

    private boolean isSetMove(int position) {
        return (movePrevInSet && position==prevIndex) || (moveNextInSet && position==nextIndex);
    }

    private boolean isMenuMove(int position) {
        Log.d(TAG,"isMenuMove(): position="+position+"  prevIndex="+ prevIndex+ "  movePrevInMenu="+movePrevInMenu+"  nextIndex="+nextIndex+"  moveNextInMenu="+moveNextInMenu);
        return (movePrevInMenu && position==prevIndex) || (moveNextInMenu && position==nextIndex);
    }

    public void showAndHide() {
        // If in using song menu, don't proceed unless user has switched on this preference
        // If in a set, only show if showNext/showPrev is selected
        if (moveNextInSet || prevNextSongMenu) {
            if (showNext && !next.getText().toString().isEmpty() && !nextVisible) {
                nextVisible = true;
                next.removeCallbacks(hideNextRunnable);
                next.show();
                next.postDelayed(hideNextRunnable, 3000);
            }
            if (showPrev && !prev.getText().toString().isEmpty() && !prevVisible) {
                prevVisible = true;
                prev.removeCallbacks(hidePrevRunnable);
                prev.show();
                prev.postDelayed(hidePrevRunnable, 3000);
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

    public void setSwipeDirection(String swipeDirection) {
        this.swipeDirection = swipeDirection;
    }
    public String getSwipeDirection() {
        return swipeDirection;
    }
}
