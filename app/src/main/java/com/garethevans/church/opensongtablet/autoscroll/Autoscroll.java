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
    private final Context c;
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
    private float scrollIncrement;
    private float scrollPosition;
    private float scrollCount;
    private float scrollIncrementScale;
    private final LinearLayout autoscrollView;
    private MyZoomLayout myZoomLayout;
    private MyRecyclerView myRecyclerView;
    private boolean usingZoomLayout;
    private final MaterialTextView autoscrollTimeText, autoscrollTotalTimeText;
    private Timer timer;
    private TimerTask timerTask;
    private String currentTimeString, totalTimeString;

    public Autoscroll(Context c, MaterialTextView autoscrollTimeText,
                      MaterialTextView autoscrollTotalTimeText, LinearLayout autoscrollView) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
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

    public void initialiseSongAutoscroll(int songHeight, int displayHeight) {
        this.displayHeight = displayHeight;
        this.songHeight = songHeight;
        autoscrollAutoStart = mainActivityInterface.getPreferences().getMyPreferenceBoolean(
                "autoscrollAutoStart", false);
        autoscrollUseDefaultTime = mainActivityInterface.getPreferences().getMyPreferenceBoolean(
                "autoscrollUseDefaultTime", true);
        autoscrollDefaultSongPreDelay = mainActivityInterface.getPreferences().getMyPreferenceInt(
                "autoscrollDefaultSongPreDelay", 20);
        autoscrollDefaultSongLength = mainActivityInterface.getPreferences().getMyPreferenceInt(
                "autoscrollDefaultSongLength", 180);
        autoscrollView.setOnClickListener(view -> isPaused = !isPaused);
        autoscrollView.setOnLongClickListener(view -> {
            stopAutoscroll();
            return true;
        });
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
        scrollIncrementScale = 1f;
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

            setupTimer();

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
                setupTimer();
            }
            isPaused = false;
            autoscrollActivated = true;
            setIsAutoscrolling(true);
            autoscrollView.post(() -> mainActivityInterface.updateOnScreenInfo("showhide"));
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
    private void setupTimer() {
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
                                scrollCount = scrollCount + (scrollIncrement*scrollIncrementScale);
                                if ((Math.max(scrollPosition,myZoomLayout.getScrollPos()) -
                                        Math.min(scrollPosition,myZoomLayout.getScrollPos()))<1) {
                                    // Don't get stuck on float rounding being compounded - use the scroll count
                                    scrollPosition = scrollCount;
                                } else {
                                    // We've moved (more than 1px), so get the actual start position
                                    scrollPosition = myZoomLayout.getScrollPos() + (scrollIncrement*scrollIncrementScale);
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
                                scrollCount = scrollCount + (scrollIncrement*scrollIncrementScale);
                                if ((Math.max(scrollPosition,myRecyclerView.getScrollY()) -
                                        Math.min(scrollPosition,myRecyclerView.getScrollY()))<1) {
                                    // Don't get stuck on float rounding being compounded - use the scroll count
                                    scrollPosition = scrollCount;
                                } else {
                                    // We've moved (more than 1px), so get the actual start position
                                    scrollPosition = myRecyclerView.getScrollY() + (scrollIncrement*scrollIncrementScale);
                                    scrollCount = scrollPosition;
                                }
                                myRecyclerView.doScrollBy(scrollIncrement*scrollIncrementScale,updateTime/2);
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
    public boolean getAutoscrollActivated() {
        return autoscrollActivated;
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
            myRecyclerView.setMaxScrollY((songHeight-displayHeight));
        }

        if (scrollHeight>0) {
            // The scroll happens every 60ms (updateTime).
            // The number of times this will happen is calculated as follows
            float numberScrolls = ((songDuration-songDelay)*1000f)/updateTime;
            // The scroll distance for each scroll is calculated as follows
            scrollIncrement = (float)scrollHeight / numberScrolls;
        } else {
            scrollIncrement = 0;
        }

        flashCount = 0;
    }

    public void speedUpAutoscroll() {
        // This increases the increment by 25%
        scrollIncrementScale = 1.25f * scrollIncrementScale;
    }
    public void slowDownAutoscroll() {
        // This decreases the increment by 25%
        scrollIncrementScale = 0.75f * scrollIncrementScale;
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
    public void checkLinkAudio(MaterialButton materialButton, MaterialEditText durationText,
                                MaterialEditText delayText, final int delay) {
        // If link audio is set and time is valid get it and set the button action
        if (mainActivityInterface.getSong().getLinkaudio()!=null &&
                !mainActivityInterface.getSong().getLinkaudio().isEmpty()) {
            Uri uri = mainActivityInterface.getStorageAccess().fixLocalisedUri(mainActivityInterface.getSong().getLinkaudio());
            if (uri!=null && mainActivityInterface.getStorageAccess().uriExists(uri)) {
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