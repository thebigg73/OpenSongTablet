package com.garethevans.church.opensongtablet.autoscroll;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.View;
import android.widget.LinearLayout;

import com.garethevans.church.opensongtablet.customviews.MaterialEditText;
import com.garethevans.church.opensongtablet.customviews.MyRecyclerView;
import com.garethevans.church.opensongtablet.customviews.MyZoomLayout;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.Timer;
import java.util.TimerTask;

public class Autoscroll {

    private boolean isAutoscrolling, wasScrolling, autoscrollOK, isPaused = false, showOn = true,
            autoscrollAutoStart, autoscrollActivated = false, autoscrollUseDefaultTime;
    private final MainActivityInterface mainActivityInterface;
    private final String TAG = "Autoscroll";
    private int songDelay;
    private int songDuration;
    private int displayHeight;
    private int songHeight;
    private int scrollTime;
    private int flashCount;
    private final int flashTime = 600;
    private int autoscrollDefaultSongLength;
    private int autoscrollDefaultSongPreDelay;
    private int colorOn;
    private final int updateTime = 60;
    private float initialScrollIncrement;
    private float scrollIncrement;
    private float scrollPosition;
    private float scrollCount;
    private final LinearLayout autoscrollView;
    private MyZoomLayout myZoomLayout;
    private MyRecyclerView myRecyclerView;
    private boolean usingZoomLayout;
    private final MaterialTextView autoscrollTimeText, autoscrollTotalTimeText;
    private Timer timer;
    private TimerTask timerTask;
    private String currentTimeString, totalTimeString;

    public Autoscroll(MainActivityInterface mainActivityInterface, MaterialTextView autoscrollTimeText,
                      MaterialTextView autoscrollTotalTimeText, LinearLayout autoscrollView) {
        this.mainActivityInterface = mainActivityInterface;
        this.autoscrollView = autoscrollView;
        this.autoscrollTimeText = autoscrollTimeText;
        this.autoscrollTotalTimeText = autoscrollTotalTimeText;
    }
    // The setters
    public void setIsAutoscrolling(boolean isAutoscrolling) {
        this.isAutoscrolling = isAutoscrolling;
    }
    public void setWasScrolling(boolean wasScrolling) {
        this.wasScrolling = wasScrolling;
    }
    public void setAutoscrollOK(boolean autoscrollOK) {
        this.autoscrollOK = autoscrollOK;
    }

    public void initialiseAutoscroll(MyZoomLayout myZoomLayout, MyRecyclerView myRecyclerView) {
        colorOn = mainActivityInterface.getMyThemeColors().getExtraInfoTextColor();
        this.myZoomLayout = myZoomLayout;
        this.myRecyclerView = myRecyclerView;
    }

    public void initialiseSongAutoscroll(Context c, int songHeight, int displayHeight) {
        this.displayHeight = displayHeight;
        this.songHeight = songHeight;
        autoscrollAutoStart = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,
                "autoscrollAutoStart", false);
        autoscrollUseDefaultTime = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,
                "autoscrollUseDefaultTime", true);
        autoscrollDefaultSongPreDelay = mainActivityInterface.getPreferences().getMyPreferenceInt(c,
                "autoscrollDefaultSongPreDelay", 20);
        autoscrollDefaultSongLength = mainActivityInterface.getPreferences().getMyPreferenceInt(c,
                "autoscrollDefaultSongLength", 180);
        autoscrollView.setOnClickListener(view -> isPaused = !isPaused);
        autoscrollView.setOnLongClickListener(view -> {
            stopAutoscroll();
            return true;
        });
        autoscrollView.setBackgroundColor(mainActivityInterface.getMyThemeColors().getPageButtonsSplitColor());
        autoscrollView.setAlpha(mainActivityInterface.getMyThemeColors().getPageButtonsSplitAlpha());
    }

    public boolean getIsPaused() {
        return isPaused;
    }
    public void pauseAutoscroll() {
        isPaused = !isPaused;
    }

    private void figureOutTimes() {
        scrollPosition = 0;
        scrollCount = 0;
        scrollTime = 0;
        songDelay = stringToInt(mainActivityInterface.getSong().getAutoscrolldelay());
        songDuration = stringToInt(mainActivityInterface.getSong().getAutoscrolllength());
        if (songDuration==0 && autoscrollUseDefaultTime) {
            songDelay = autoscrollDefaultSongPreDelay;
            songDuration = autoscrollDefaultSongLength;
        }

        if (songDuration>0) {
            // We have valid times, so good to go.  Calculate the autoscroll values
            //float numberScrolls = ((songDuration - songDelay) * 1000f) / updateTime;

            calculateAutoscroll();
            resetTimers();

            totalTimeString = " / " + mainActivityInterface.getTimeTools().timeFormatFixer(songDuration);
            autoscrollTotalTimeText.post(() -> autoscrollTotalTimeText.setText(totalTimeString));

            setupTimer(mainActivityInterface);

            setAutoscrollOK(true);

            if (autoscrollActivated && autoscrollAutoStart) {
                // We have already initiated scrolling and want it to start automatically
                startAutoscroll();
            }
        }
    }

    public void startAutoscroll() {
        // If we are in Performance mode using a normal OpenSong song, we scroll the ZoomLayout
        // If we are in Stage mode, or viewing a pdf, we scroll the RecyclerView
        usingZoomLayout = mainActivityInterface.getMode().equals("Performance") && mainActivityInterface.getSong().getFiletype().equals("XML");

        if (usingZoomLayout) {
            myZoomLayout.setIsUserTouching(false);
            myZoomLayout.scrollTo(0,0);
        } else {
            myRecyclerView.setUserTouching(false);
            myRecyclerView.scrollToTop();
        }

        figureOutTimes();

        if (autoscrollOK) {
            if (timer==null) {
                timer = new Timer();
            }
            if (timerTask==null) {
                setupTimer(mainActivityInterface);
            }
            isPaused = false;
            autoscrollActivated = true;
            setIsAutoscrolling(true);
            autoscrollView.post(() -> autoscrollView.setVisibility(View.VISIBLE));
            try {
                timer.scheduleAtFixedRate(timerTask, 0, updateTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void stopAutoscroll() {
        // Force stopped, so reset activated
        autoscrollActivated = false;
        // Now end
        endAutoscroll();
    }
    private void endAutoscroll() {
        // Called at normal end.  Doesn't reset activated
        setIsAutoscrolling(false);
        autoscrollView.postDelayed(() -> autoscrollView.setVisibility(View.GONE),1000);
        resetTimers();
    }

    private void resetTimers() {
        if (timerTask != null) {
            timerTask.cancel();
        }
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }
    private void setupTimer(MainActivityInterface mainActivityInterface) {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                flashCount = flashCount + updateTime;
                if (flashCount>flashTime) {
                    showOn = !showOn;
                    flashCount = 0;
                }
                if (!isPaused) {
                    autoscrollTimeText.setTextColor(colorOn);
                    currentTimeString = mainActivityInterface.getTimeTools().timeFormatFixer((int)((float)scrollTime/1000f));
                    if (scrollTime < songDelay*1000f) {
                        // This is predelay, so set the alpha down
                        autoscrollTimeText.post(() -> {
                            autoscrollTimeText.setAlpha(0.6f);
                            autoscrollTimeText.setText(currentTimeString);
                            // Listen out for scroll changes
                            if (usingZoomLayout) {
                                if (scrollPosition != myZoomLayout.getScrollPos() ||
                                        scrollCount != myZoomLayout.getScrollPos()) {
                                    scrollPosition = myZoomLayout.getScrollPos();
                                    scrollCount = scrollPosition;
                                }
                            } else {
                                if (scrollPosition != myRecyclerView.getScrollY() ||
                                scrollCount != myRecyclerView.getScrollY()) {
                                    scrollPosition = myRecyclerView.getScrollY();
                                    scrollCount = scrollPosition;
                                }
                            }
                        });
                    } else {
                        // Fix the alpha back and set the text
                        autoscrollTimeText.post(() -> {
                            autoscrollTimeText.setAlpha(1.0f);
                            autoscrollTimeText.setText(currentTimeString);
                        });

                        // Do the scroll as long as the user isn't touching the screen
                        if (usingZoomLayout && !myZoomLayout.getIsUserTouching()) {

                            // Rather than assume the scroll position (as it could've been manually dragged)
                            // Compare the scroll count (float) and scroll position (int).
                            myZoomLayout.post(() -> {
                                // Check for scaling changes
                                calculateAutoscroll();
                                scrollCount = scrollCount + scrollIncrement;
                                if ((Math.max(scrollPosition,myZoomLayout.getScrollPos()) -
                                        Math.min(scrollPosition,myZoomLayout.getScrollPos()))<1) {
                                    // Don't get stuck on float rounding being compounded - use the scroll count
                                    scrollPosition = scrollCount;
                                } else {
                                    // We've moved (more than 1px), so get the actual start position
                                    scrollPosition = myZoomLayout.getScrollPos() + scrollIncrement;
                                    scrollCount = scrollPosition;
                                }
                                //myZoomLayout.smoothScrollTo(0, (int) scrollPosition, updateTime);
                                myZoomLayout.autoscrollTo(scrollCount);
                            });

                        } else if (!myRecyclerView.getIsUserTouching()){
                            // Rather than assume the scroll position (as it could've been manually dragged)
                            // Compare the scroll count (float) and scroll position (int).
                            myRecyclerView.post(() -> {
                                // Check for scaling changes
                                calculateAutoscroll();
                                scrollCount = scrollCount + scrollIncrement;
                                if ((Math.max(scrollPosition,myRecyclerView.getScrollY()) -
                                        Math.min(scrollPosition,myRecyclerView.getScrollY()))<1) {
                                    // Don't get stuck on float rounding being compounded - use the scroll count
                                    scrollPosition = scrollCount;
                                } else {
                                    // We've moved (more than 1px), so get the actual start position
                                    scrollPosition = myRecyclerView.getScrollY() + scrollIncrement;
                                    scrollCount = scrollPosition;
                                }
                                myRecyclerView.doScrollBy((int)scrollIncrement,updateTime/2);
                            });
                        }
                    }
                    scrollTime = scrollTime + updateTime;
                    float scaledHeight;
                    if (usingZoomLayout) {
                        scaledHeight = songHeight*myZoomLayout.getScaleFactor();
                        if (Math.ceil(scrollPosition) >= (scaledHeight - displayHeight)) {
                            // Scrolling is done as we've reached the end
                            endAutoscroll();
                        }
                    } else {
                        if (myRecyclerView.getScrolledToBottom())
                            // Scrolling is done as we've reached the end
                            endAutoscroll();
                    }


                } else {
                    autoscrollTimeText.post(() -> {
                        if (showOn) {
                            autoscrollTimeText.setTextColor(colorOn);
                        } else {
                            autoscrollTimeText.setTextColor(Color.TRANSPARENT);
                        }
                    });

                }
            }
        };
    }

    // The getters
    public boolean getIsAutoscrolling() {
        return isAutoscrolling;
    }
    public boolean getWasScrolling() {
        return wasScrolling;
    }
    public boolean getAutoscrollOK() {
        return autoscrollOK;
    }

    // The calculations
    private void calculateAutoscroll() {
        // The total scroll amount is the height of the view - the screen height.
        // If this is less than 0, no scrolling is required.
        int scrollHeight;
        if (usingZoomLayout) {
            scrollHeight = (int) (songHeight * myZoomLayout.getScaleFactor()) - displayHeight;
        } else {
            scrollHeight = songHeight - displayHeight;
            myRecyclerView.setMaxScrollY(scrollHeight);
        }

        if (scrollHeight>0) {
            // The scroll happens every 60ms (updateTime).
            // The number of times this will happen is calculated as follows
            float numberScrolls = ((songDuration-songDelay)*1000f)/updateTime;
            // The scroll distance for each scroll is calculated as follows
            scrollIncrement = (float)scrollHeight / numberScrolls;
            initialScrollIncrement = scrollIncrement;
        } else {
            scrollIncrement = 0;
            initialScrollIncrement = 0;
        }

        flashCount = 0;
    }

    public void speedUpAutoscroll() {
        // This increases the increment by 25%
        scrollIncrement = 1.25f * scrollIncrement;
        initialScrollIncrement = 1.25f * initialScrollIncrement;
    }
    public void slowDownAutoscroll() {
        // This decreases the increment by 25%
        scrollIncrement = 0.75f * scrollIncrement;
        initialScrollIncrement = 0.75f * initialScrollIncrement;
    }
    private int stringToInt(String string) {
        if (string!=null && !string.isEmpty()) {
            try {
                return Integer.parseInt(string);
            } catch (Exception e) {
                return 0;
            }
        } else {
            return 0;
        }
    }

    // This is called from both the Autoscroll settings and bottom sheet to activate the link audio button
    public void checkLinkAudio(Context c, MainActivityInterface mainActivityInterface,
                                MaterialButton materialButton, MaterialEditText durationText,
                                MaterialEditText delayText, final int delay) {
        // If link audio is set and time is valid get it and set the button action
        if (mainActivityInterface.getSong().getLinkaudio()!=null &&
                !mainActivityInterface.getSong().getLinkaudio().isEmpty()) {
            Uri uri = mainActivityInterface.getStorageAccess().fixLocalisedUri(c,
                    mainActivityInterface, mainActivityInterface.getSong().getLinkaudio());
            if (uri!=null && mainActivityInterface.getStorageAccess().uriExists(c, uri)) {
                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(c, uri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(mediaPlayer1 -> materialButton.setOnClickListener(v -> {
                    // Updating the text fields triggers the listener which saves
                    int duration = mediaPlayer1.getDuration()/1000;
                    if (delay > duration) {
                        delayText.setText(duration+"");
                    }
                    durationText.setText(""+duration);
                    materialButton.setVisibility(View.VISIBLE);
                }));
            } else {
                materialButton.setVisibility(View.GONE);
            }
        }
    }

    public void stopTimers() {
        resetTimers();
        timer = null;
        timerTask = null;
    }
}

// TODO Learn autoscroll should we want it back - probably not used very much anyway