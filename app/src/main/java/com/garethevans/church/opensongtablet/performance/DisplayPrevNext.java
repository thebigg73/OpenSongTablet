package com.garethevans.church.opensongtablet.performance;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MyFAB;
import com.garethevans.church.opensongtablet.customviews.MyZoomLayout;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DisplayPrevNext {
    // This deals with showing the previous and next song buttons and their actions
    // Even if we don't display the buttons, this sets up what to do on next/prev action (swipe, pedal, etc)

    private final MainActivityInterface mainActivityInterface;
    private final Context c;
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final String TAG = "DisplayPrevNext";
    private final RelativeLayout layout;
    private MyZoomLayout zoomLayout;
    private final ExtendedFloatingActionButton prev, next;
    private final MyFAB prevFAB, nextFAB;
    private boolean showPrev, prevVisible = false;
    private boolean showNext, nextVisible = false;
    private boolean prevNextSongMenu;
    private boolean prevNextTextButtons;
    private boolean prevNextHide;
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
    private final Runnable hideNextFABRunnable = new Runnable() {
        @Override
        public void run() {
            nextFAB.hide();
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
    private final Runnable hidePrevFABRunnable = new Runnable() {
        @Override
        public void run() {
            prevFAB.hide();
            prevVisible = false;
        }
    };

    public DisplayPrevNext (Context c, RelativeLayout layout,
                            ExtendedFloatingActionButton prev, ExtendedFloatingActionButton next,
                            MyFAB prevFAB, MyFAB nextFAB) {
        this.c = c;
        this.mainActivityInterface = (MainActivityInterface) c;
        this.layout = layout;
        this.prev = prev;
        this.next = next;
        this.prevFAB = prevFAB;
        this.nextFAB = nextFAB;
        updateShow();
        updateColors();
    }

    public void setZoomLayout(MyZoomLayout zoomLayout) {
        this.zoomLayout = zoomLayout;
    }

    public void updateShow() {
        showPrev = mainActivityInterface.getPreferences().getMyPreferenceBoolean("prevInSet", false);
        showNext = mainActivityInterface.getPreferences().getMyPreferenceBoolean("nextInSet", true);
        prevNextSongMenu = mainActivityInterface.getPreferences().getMyPreferenceBoolean("prevNextSongMenu", false);
        prevNextTextButtons = mainActivityInterface.getPreferences().getMyPreferenceBoolean("prevNextTextButtons", true);
        prevNextHide = mainActivityInterface.getPreferences().getMyPreferenceBoolean("prevNextHide",true);
        boolean pageButtonMini = mainActivityInterface.getPreferences().getMyPreferenceBoolean("pageButtonMini", false);
        prevFAB.setSize(pageButtonMini ? FloatingActionButton.SIZE_MINI:FloatingActionButton.SIZE_NORMAL);
        nextFAB.setSize(pageButtonMini ? FloatingActionButton.SIZE_MINI:FloatingActionButton.SIZE_NORMAL);

        // Decide which layout is required and hide the other one
        layout.findViewById(R.id.nextPrevInfoLayout).setVisibility(prevNextTextButtons &&
                !mainActivityInterface.getSettingsOpen() &&
                !mainActivityInterface.getMode().equals(c.getString(R.string.presenter_mode)) ?
                View.VISIBLE:View.GONE);
        layout.findViewById(R.id.nextPrevInfoFABLayout).setVisibility(prevNextTextButtons &&
                !mainActivityInterface.getSettingsOpen() &&
                !mainActivityInterface.getMode().equals(c.getString(R.string.presenter_mode)) ?
                View.GONE:View.VISIBLE);

        prev.hide();
        prevFAB.hide();
        next.hide();
        nextFAB.hide();
        nextVisible = false;
        prevVisible = false;

        if (showPrev && !mainActivityInterface.getMenuOpen()) {
            layout.findViewById(R.id.prevHolder).setVisibility(prevNextTextButtons?View.VISIBLE:View.GONE);
            layout.findViewById(R.id.prevFABHolder).setVisibility(prevNextTextButtons?View.GONE:View.VISIBLE);
        } else {
            layout.findViewById(R.id.prevHolder).setVisibility(View.GONE);
            layout.findViewById(R.id.prevFABHolder).setVisibility(View.GONE);

        }
        if (showNext && !mainActivityInterface.getMenuOpen()) {
            layout.findViewById(R.id.nextHolder).setVisibility(prevNextTextButtons?View.VISIBLE:View.GONE);
            layout.findViewById(R.id.nextFABHolder).setVisibility(prevNextTextButtons?View.GONE:View.VISIBLE);
        } else {
            layout.findViewById(R.id.nextHolder).setVisibility(View.GONE);
            layout.findViewById(R.id.nextFABHolder).setVisibility(View.GONE);
        }
    }

    public boolean getShowPrev() {
        return showPrev;
    }
    public boolean getShowNext() {
        return showNext;
    }

    public boolean getTextButtons() {
        return prevNextTextButtons;
    }

    public void setPrevNext() {
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
            prevFAB.setOnClickListener(v -> moveToPrev());
            next.setOnClickListener(v -> moveToNext());
            nextFAB.setOnClickListener(v -> moveToNext());

            // Update the text
            next.setText(nextText);
            prev.setText(previousText);

            // This shows the ones chosen if not empty, then hides again
            showAndHide();
        }
    }

    public void getPositions() {
        int setPosition = mainActivityInterface.getSetActions().indexSongInSet(mainActivityInterface.getSong());
        mainActivityInterface.getCurrentSet().setIndexSongInSet(setPosition);
        int songPosition = mainActivityInterface.getPositionOfSongInMenu();

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
            if (songPosition>=0 && songPosition < mainActivityInterface.getSongsInMenu().size()-1) {
                nextIndex = songPosition + 1;
                moveNextInMenu = true;
            } else {
                nextIndex = -1;
                moveNextInMenu = false;
            }
        }
    }

    private String getTextForButton(int position) {
        String text = "";
        if (position>-1) {
            if (isSetMove(position)) {
                if (position < mainActivityInterface.getCurrentSet().getSetItems().size()) {
                    text = mainActivityInterface.getCurrentSet().getFilename(position);

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
            stopFlingScroll();
            doMove(nextIndex);
        } else {
            mainActivityInterface.getShowToast().doIt(c.getString(R.string.last_song));
        }
    }
    public void moveToPrev() {
        swipeDirection = "L2R";
        if (prevIndex != -1) {
            stopFlingScroll();
            doMove(prevIndex);
        } else {
            mainActivityInterface.getShowToast().doIt(c.getString(R.string.first_song));
        }
    }

    private void stopFlingScroll() {
        if (zoomLayout!=null) {
            try {
                zoomLayout.stopFlingScroll();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void doMove(int position) {
        prev.removeCallbacks(hidePrevRunnable);
        prevFAB.removeCallbacks(hidePrevFABRunnable);
        next.removeCallbacks(hideNextRunnable);
        nextFAB.removeCallbacks(hideNextFABRunnable);
        prev.hide();
        prevFAB.hide();
        next.hide();
        nextFAB.hide();
        nextVisible = false;
        prevVisible = false;
        if (isSetMove(position)) {
            mainActivityInterface.loadSongFromSet(position);
        } else if (isMenuMove(position)){
            mainActivityInterface.doSongLoad(mainActivityInterface.getSongInMenu(position).getFolder(),
                    mainActivityInterface.getSongInMenu(position).getFilename(),true);
        }
    }

    private boolean isSetMove(int position) {
        return (movePrevInSet && position==prevIndex) || (moveNextInSet && position==nextIndex);
    }

    private boolean isMenuMove(int position) {
        return (movePrevInMenu && position==prevIndex) || (moveNextInMenu && position==nextIndex);
    }

    public void showAndHide() {
        // If in using song menu, don't proceed unless user has switched on this preference
        // If in a set, only show if showNext/showPrev is selected
        if (moveNextInSet || movePrevInSet || prevNextSongMenu) {
            if (showNext && !next.getText().toString().isEmpty() && !nextVisible) {
                nextVisible = true;
                if (prevNextTextButtons) {
                    next.removeCallbacks(hideNextRunnable);
                    next.show();
                    if (prevNextHide) {
                        next.postDelayed(hideNextRunnable, 3000);
                    }
                } else {
                    nextFAB.removeCallbacks(hideNextFABRunnable);
                    nextFAB.show();
                    if (prevNextHide) {
                        nextFAB.postDelayed(hideNextFABRunnable, 3000);
                    }
                }
            }
            if (showPrev && !prev.getText().toString().isEmpty() && !prevVisible) {
                prevVisible = true;
                if (prevNextTextButtons) {
                    prev.removeCallbacks(hidePrevRunnable);
                    prev.show();
                    if (prevNextHide) {
                        prev.postDelayed(hidePrevRunnable, 3000);
                    }
                } else {
                    prevFAB.removeCallbacks(hidePrevFABRunnable);
                    prevFAB.show();
                    if (prevNextHide) {
                        prevFAB.postDelayed(hidePrevFABRunnable, 3000);
                    }
                }
            }
        }
    }

    public void updateColors() {
        float buttonAlpha = mainActivityInterface.getMyThemeColors().getPageButtonsSplitAlpha();
        int buttonColor = mainActivityInterface.getMyThemeColors().getPageButtonsSplitColor();
        int buttonIconColor = mainActivityInterface.getMyThemeColors().getExtraInfoTextColor();
        prev.setIconTint(ColorStateList.valueOf(buttonIconColor));
        next.setIconTint(ColorStateList.valueOf(buttonIconColor));
        Drawable leftArrow = ContextCompat.getDrawable(c, R.drawable.arrow_left);
        Drawable rightArrow = ContextCompat.getDrawable(c, R.drawable.arrow_right);
        if (leftArrow!=null) {
            DrawableCompat.setTint(leftArrow, buttonIconColor);
            prevFAB.setImageDrawable(leftArrow);
        }
        if (rightArrow!=null) {
            DrawableCompat.setTint(rightArrow, buttonIconColor);
            nextFAB.setImageDrawable(rightArrow);
        }
        prev.setBackgroundTintList(ColorStateList.valueOf(buttonColor));
        next.setBackgroundTintList(ColorStateList.valueOf(buttonColor));
        prevFAB.setBackgroundTintList(ColorStateList.valueOf(buttonColor));
        nextFAB.setBackgroundTintList(ColorStateList.valueOf(buttonColor));
        layout.setAlpha(buttonAlpha);
    }

    public void setSwipeDirection(String swipeDirection) {
        this.swipeDirection = swipeDirection;
    }
    public String getSwipeDirection() {
        return swipeDirection;
    }
}
