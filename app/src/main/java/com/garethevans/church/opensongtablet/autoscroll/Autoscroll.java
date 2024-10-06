package com.garethevans.church.opensongtablet.autoscroll;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MyMaterialEditText;
import com.garethevans.church.opensongtablet.customviews.MyRecyclerView;
import com.garethevans.church.opensongtablet.customviews.MyZoomLayout;
import com.garethevans.church.opensongtablet.customviews.OnScreenInfo;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Autoscroll {

    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "Autoscroll", mode_performance;
    private boolean isAutoscrolling, autoscrollOK, isPaused = false, showOn = true, alreadyFiguredOut,
            autoscrollAutoStart, autoscrollActivated = false, autoscrollUseDefaultTime,
            onscreenAutoscrollHide, usingZoomLayout, autoscrollPreDelayCountdown, autoHideSent=false;
    private int songDelay, songDuration, displayWidth, displayHeight, songWidth, songHeight, scrollTime, flashCount,
            autoscrollDefaultSongLength, autoscrollDefaultSongPreDelay, colorOn, inlinePauseTotal;
    private final int flashTime = 600, updateTime = 20;
    private float scrollIncrement, scrollPosition, scrollCount, scrollIncrementScale;
    private final OnScreenInfo onScreenInfo;
    private final LinearLayout autoscrollView;
    private MyZoomLayout myZoomLayout;
    private MyRecyclerView myRecyclerView;
    private final MaterialTextView autoscrollTimeText, autoscrollTotalTimeText;
    private ScheduledExecutorService scheduledExecutorService;
    private Runnable scrollRunnable;
    private ScheduledFuture<?> task;
    private String currentTimeString, totalTimeString;
    private ArrayList<InlinePause> inlinePauses;
    private int inlinePauseStartTime, inlinePauseEndTime;

    // Initialise the autoscroll class from MainActivity and receive the time box vierws
    public Autoscroll(Context c, OnScreenInfo onScreenInfo, MaterialTextView autoscrollTimeText,
                      MaterialTextView autoscrollTotalTimeText, LinearLayout autoscrollView) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        this.onScreenInfo = onScreenInfo;
        this.autoscrollView = autoscrollView;
        this.autoscrollTimeText = autoscrollTimeText;
        this.autoscrollTotalTimeText = autoscrollTotalTimeText;
        mode_performance = c.getString(R.string.mode_performance);
    }
    public void setupAutoscrollPreferences() {
        autoscrollAutoStart = mainActivityInterface.getPreferences().getMyPreferenceBoolean(
                "autoscrollAutoStart", false);
        autoscrollUseDefaultTime = mainActivityInterface.getPreferences().getMyPreferenceBoolean(
                "autoscrollUseDefaultTime", true);
        autoscrollDefaultSongPreDelay = mainActivityInterface.getPreferences().getMyPreferenceInt(
                "autoscrollDefaultSongPreDelay", 20);
        autoscrollDefaultSongLength = mainActivityInterface.getPreferences().getMyPreferenceInt(
                "autoscrollDefaultSongLength", 180);
        onscreenAutoscrollHide = mainActivityInterface.getPreferences().getMyPreferenceBoolean(
                "onscreenAutoscrollHide",true);
        autoscrollPreDelayCountdown = mainActivityInterface.getPreferences().getMyPreferenceBoolean(
                "autoscrollPreDelayCountdown",false);
    }


    // Initialise the song views from PerformanceFragment with the views to scroll
    public void initialiseAutoscroll(MyZoomLayout myZoomLayout, MyRecyclerView myRecyclerView) {
        this.myZoomLayout = myZoomLayout;
        this.myRecyclerView = myRecyclerView;
        if (mainActivityInterface!=null) {
            colorOn = mainActivityInterface.getMyThemeColors().getExtraInfoTextColor();
            this.myRecyclerView.initialiseRecyclerView(mainActivityInterface);
        }
    }
    // Receive the view sizes from PerformanceFragment so we can calculate the autoscroll
    public void initialiseSongAutoscroll(int songWidth, int songHeight, int displayWidth, int displayHeight) {
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.songWidth = songWidth;
        this.songHeight = songHeight;
        alreadyFiguredOut = false;
        autoscrollView.setOnClickListener(view -> {
            mainActivityInterface.getNearbyConnections().sendAutoscrollPausePayload();
            isPaused = !isPaused;
        });
        autoscrollView.setOnLongClickListener(view -> {
            stopAutoscroll();
            return true;
        });
        setupAutoscrollPreferences();
    }


    // The setters
    public void setIsAutoscrolling(boolean isAutoscrolling) {
        this.isAutoscrolling = isAutoscrolling;
    }
    public void setAutoscrollOK(boolean autoscrollOK) {
        this.autoscrollOK = autoscrollOK;
    }
    public void setAutoscrollAutoStart(boolean autoscrollAutoStart) {
        this.autoscrollAutoStart = autoscrollAutoStart;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("autoscrollAutoStart",autoscrollAutoStart);
    }
    public void setAutoscrollUseDefaultTime(boolean autoscrollUseDefaultTime) {
        this.autoscrollUseDefaultTime = autoscrollUseDefaultTime;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("autoscrollUseDefaultTime",autoscrollUseDefaultTime);
    }
    public void setAutoscrollDefaultSongPreDelay(int autoscrollDefaultSongPreDelay) {
        this.autoscrollDefaultSongPreDelay = autoscrollDefaultSongPreDelay;
        mainActivityInterface.getPreferences().setMyPreferenceInt("autoscrollDefaultSongPreDelay",autoscrollDefaultSongPreDelay);
    }
    public void setAutoscrollDefaultSongLength(int autoscrollDefaultSongLength) {
        this.autoscrollDefaultSongLength = autoscrollDefaultSongLength;
        mainActivityInterface.getPreferences().setMyPreferenceInt("autoscrollDefaultSongLength",autoscrollDefaultSongLength);
    }
    public void setAutoscrollActivated(boolean autoscrollActivated) {
        this.autoscrollActivated = autoscrollActivated;
    }
    public void setAutoscrollPreDelayCountdown(boolean autoscrollPreDelayCountdown) {
        this.autoscrollPreDelayCountdown = autoscrollPreDelayCountdown;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("autoscrollPreDelayCountdown",autoscrollPreDelayCountdown);
    }

    // The getters
    public boolean getAutoscrollAutoStart() {
        return autoscrollAutoStart;
    }
    public boolean getAutoscrollUseDefaultTime() {
        return autoscrollUseDefaultTime;
    }
    public int getAutoscrollDefaultSongPreDelay() {
        return autoscrollDefaultSongPreDelay;
    }
    public int getAutoscrollDefaultSongLength() {
        return autoscrollDefaultSongLength;
    }
    public boolean getOnscreenAutoscrollHide() {
        return onscreenAutoscrollHide;
    }
    public boolean getIsPaused() {
        return isPaused;
    }
    public boolean getShouldAutostart() {
        return autoscrollActivated && autoscrollAutoStart;
    }
    public boolean getIsAutoscrolling() {
        return isAutoscrolling;
    }
    public boolean getAutoscrollActivated() {
        return autoscrollActivated;
    }
    public boolean finishedPreDelay() {
        return scrollTime >= (songDelay*1000f);
    }
    public boolean getAutoscrollPreDelayCountdown() {
        return autoscrollPreDelayCountdown;
    }


    // This is called from both the Autoscroll settings and bottom sheet to activate the link audio button
    public void checkLinkAudio(MaterialButton fromLinkButton, MyMaterialEditText minText, MyMaterialEditText secText,
                               MyMaterialEditText delayText, final int delay) {
        // If link audio is set and time is valid get it and set the button action
        if (mainActivityInterface.getSong().getLinkaudio()!=null &&
                !mainActivityInterface.getSong().getLinkaudio().isEmpty()) {
            Uri uri = mainActivityInterface.getStorageAccess().fixLocalisedUri(mainActivityInterface.getSong().getLinkaudio());
            if (!mainActivityInterface.getSong().getLinkaudio().isEmpty() &&
                    uri!=null && mainActivityInterface.getStorageAccess().uriExists(uri)) {
                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(c, uri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mediaPlayer.prepareAsync();
                fromLinkButton.setVisibility(View.VISIBLE);

                mediaPlayer.setOnPreparedListener(mediaPlayer1 -> fromLinkButton.setOnClickListener(v -> {
                    // Updating the text fields triggers the listener which saves
                    int duration = mediaPlayer1.getDuration()/1000;
                    if (delay > duration) {
                        delayText.setText("0");
                    }
                    int[] time = mainActivityInterface.getTimeTools().getMinsSecsFromSecs(duration);
                    minText.setText(String.valueOf(time[0]));
                    secText.setText(String.valueOf(time[1]));
                }));
            } else {
                fromLinkButton.setVisibility(View.GONE);
            }
        }
    }



    // Prepare the times for autoscrolling and setup/reset/stop the tasks
    private void initialiseScrollValues() {
        scrollPosition = 0;
        scrollCount = 0;
        scrollTime = 0;
        scrollIncrementScale = 1f;
        alreadyFiguredOut = false;
        songDelay = stringToInt(mainActivityInterface.getSong().getAutoscrolldelay());
        songDuration = stringToInt(mainActivityInterface.getSong().getAutoscrolllength());
        inlinePauses = null;
    }
    private void figureOutTimes() {
        if (songDuration==0 && autoscrollUseDefaultTime) {
            songDelay = autoscrollDefaultSongPreDelay;
            songDuration = autoscrollDefaultSongLength;
        }

        if (songDuration>0) {
            // We have valid times, so good to go.  Calculate the autoscroll values
            calculateAutoscroll();
            resetTimers();

            totalTimeString = " / " + mainActivityInterface.getTimeTools().timeFormatFixer(songDuration);
            autoscrollTotalTimeText.post(() -> autoscrollTotalTimeText.setText(totalTimeString));

            onScreenInfo.setFinishedAutoscrollPreDelay(false);
            //onScreenInfo.setFirstShowAutoscroll(true);
            autoHideSent = false;

            setupTimer();
            setAutoscrollOK(true);
        }
        alreadyFiguredOut = true;
    }
    private void setupTimer() {
        scrollRunnable = () -> {
            // The display flashes when paused
            flashCount = flashCount + updateTime;
            if (flashCount>flashTime) {
                showOn = !showOn;
                flashCount = 0;
            }

            if (!autoHideSent && onscreenAutoscrollHide && (songDelay==0 || (songDelay>0 && (scrollTime/1000f)>songDelay))) {
                autoHideSent = true;
                Log.d(TAG,"sending hide runnable");
                onScreenInfo.setFinishedAutoscrollPreDelay(true);
                autoscrollView.post(() -> mainActivityInterface.updateOnScreenInfo("showhide"));
            }

            if (!isPaused) {
                autoscrollTimeText.setTextColor(colorOn);
                currentTimeString = mainActivityInterface.getTimeTools().timeFormatFixer((int)((float)scrollTime/1000f));
                if (scrollTime < songDelay*1000f) {
                    // This is predelay, so set the alpha down
                    autoscrollTimeText.post(() -> {
                        autoscrollTimeText.setAlpha(0.6f);
                        // If user wants a countdown for predelay, show that
                        if (autoscrollPreDelayCountdown && songDelay>0) {
                            currentTimeString =  mainActivityInterface.getTimeTools().timeFormatFixer((int)(1+ songDelay - (float)scrollTime/1000f));
                        }
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
                } else if (inlinePauseStartTime!=0 && inlinePauseEndTime!=0 &&
                        scrollTime >= inlinePauseStartTime && scrollTime < inlinePauseEndTime) {
                    // We are in an inline delay period
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
                    if (inlinePauseStartTime!=0 && inlinePauseEndTime!=0 &&
                            !inlinePauses.isEmpty() &&
                            scrollTime > inlinePauseStartTime && scrollTime > inlinePauseEndTime) {
                        // Get the next delay time ready if required
                        // Delete the first entry
                        inlinePauses.remove(0);
                        if (!inlinePauses.isEmpty()) {
                            inlinePauseStartTime = inlinePauses.get(0).pauseStartTime;
                            inlinePauseEndTime = inlinePauses.get(0).pauseEndTime;
                        } else {
                            inlinePauseStartTime = 0;
                            inlinePauseEndTime = 0;
                        }
                    }

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
        };
    }
    private void calculateAutoscroll() {
        // The total scroll amount is the height of the view - the screen height.
        // If this is less than 0, no scrolling is required.
        int scrollHeight;
        int scrollWidth;
        if (usingZoomLayout) {
            //songHeight = myZoomLayout.getHeight();
            scrollWidth = (int) (songWidth * myZoomLayout.getScaleFactor()) - displayWidth;
            scrollHeight = (int) (songHeight * myZoomLayout.getScaleFactor()) - displayHeight;
        } else {
            scrollWidth = songWidth - displayWidth;
            scrollHeight = songHeight - displayHeight;
            myRecyclerView.setMaxScrollY(songHeight-displayHeight);
        }
        if (inlinePauses == null) {
            buildInlinePauseArrays();
        }

        if (mainActivityInterface.getGestures().getPdfLandscapeView()) {
            // Horizontal scrolling
            if (scrollWidth > 0) {
                // The scroll happens every 20ms (updateTime).
                // The number of times this will happen is calculated as follows
                float numberScrolls = ((songDuration - songDelay - inlinePauseTotal) * 1000f) / updateTime;
                // The scroll distance for each scroll is calculated as follows
                scrollIncrement = (float) scrollWidth / numberScrolls;
            } else {
                scrollIncrement = 0;
            }
        } else {
            // Vertical scrolling
            if (scrollHeight > 0) {
                // The scroll happens every 20ms (updateTime).
                // The number of times this will happen is calculated as follows
                float numberScrolls = ((songDuration - songDelay - inlinePauseTotal) * 1000f) / updateTime;
                // The scroll distance for each scroll is calculated as follows
                scrollIncrement = (float) scrollHeight / numberScrolls;
            } else {
                scrollIncrement = 0;
            }
        }

        flashCount = 0;
    }
    private void resetTimers() {
        if (task!=null) {
            task.cancel(true);
        }
        task = null;
        scheduledExecutorService = null;
    }
    public void stopTimers() {

        resetTimers();
        scheduledExecutorService = null;
    }


    // Control the autoscroll process (stop, start, pause, change speed, etc.)
    @SuppressLint("DiscouragedApi")
    public void startAutoscroll() {
        // Initialise the scroll positions and time
        initialiseScrollValues();

        // If we are in Performance mode using a normal OpenSong song, we scroll the ZoomLayout
        // If we are in Stage mode, or viewing a pdf, we scroll the RecyclerView
        usingZoomLayout = mainActivityInterface.getMode().equals(mode_performance) &&
                (mainActivityInterface.getSong().getFiletype().equals("XML") ||
        mainActivityInterface.getSong().getFiletype().equals("IMG"));

        if (myZoomLayout!=null && usingZoomLayout) {
            myZoomLayout.setIsUserTouching(false);
            myZoomLayout.scrollTo(0,0);
            myZoomLayout.checkScrollPosition();
        } else if (!usingZoomLayout && myRecyclerView!=null) {
            myRecyclerView.setUserTouching(false);
            myRecyclerView.scrollToTop();
            myRecyclerView.checkScrollPosition();
        }

        if (!alreadyFiguredOut) {
            figureOutTimes();
        }

        if (autoscrollOK) {
            if (scheduledExecutorService == null) {
                scheduledExecutorService = Executors.newScheduledThreadPool(1);
            }
            isPaused = false;
            autoscrollActivated = true;
            setIsAutoscrolling(true);


            autoscrollView.post(() -> {
                mainActivityInterface.updateOnScreenInfo("showhide");
                //onScreenInfo.setFirstShowAutoscroll(false);
            });


            try {
                task = scheduledExecutorService.scheduleAtFixedRate(scrollRunnable,0,updateTime, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // If we are connected as a host, send to client devices
            if (mainActivityInterface.getNearbyConnections().getIsHost() &&
            mainActivityInterface.getNearbyConnections().getUsingNearby()) {
                mainActivityInterface.getNearbyConnections().sendAutoscrollPayload("autoscroll_start");
            }
        }
    }
    public void stopAutoscroll() {
        // Force stopped, so reset activated
        autoscrollActivated = false;
        // Now end
        endAutoscroll();
        // If we are connected as a host, send to client devices
        if (mainActivityInterface.getNearbyConnections().getIsHost() &&
                mainActivityInterface.getNearbyConnections().getUsingNearby()) {
            mainActivityInterface.getNearbyConnections().sendAutoscrollPayload("autoscroll_stop");
        }
    }
    private void endAutoscroll() {
        if (usingZoomLayout && myZoomLayout!=null) {
            myZoomLayout.checkScrollPosition();
        } else if (myRecyclerView!=null) {
            myRecyclerView.checkScrollPosition();
        }

        // Called at normal end.  Doesn't reset activated
        setIsAutoscrolling(false);
        autoscrollView.postDelayed(() -> autoscrollView.setVisibility(View.GONE),1000);
        resetTimers();
    }
    public void pauseAutoscroll() {
        isPaused = !isPaused;
    }
    public void speedUpAutoscroll() {
        // This increases the increment by 25%
        scrollIncrementScale = 1.25f * scrollIncrementScale;
    }
    public void slowDownAutoscroll() {
        // This decreases the increment by 25%
        scrollIncrementScale = 0.75f * scrollIncrementScale;
    }


    public void buildInlinePauseArrays() {
        inlinePauses = new ArrayList<>();
        inlinePauseTotal = 0;
        int scrollRequired;
        int prevScrollPos = 0;
        // Get the total number of lines in the song lyrics
        String[] lyricLines = mainActivityInterface.getSong().getLyrics().split("\n");
        for (int x=0; x<lyricLines.length; x++) {
            String lyricLine = lyricLines[x];
            if (lyricLine.startsWith(";D:")) {
                lyricLine = lyricLine.replace(";D:","").replaceAll("\\D","").trim();
                if (!lyricLine.isEmpty()) {
                    InlinePause inlinePause = new InlinePause();
                    int thisTime = Integer.parseInt(lyricLine);
                    // Calculate the scroll position that this pause gets activated at
                    float percentageOfLines = (float)x/(float)lyricLines.length;
                    int howFarDownSongHeight = Math.round(percentageOfLines*songHeight);
                    if (howFarDownSongHeight<displayHeight) {
                        // If the item is likely on screen, pause happens immediately
                        inlinePause.pauseStartTime = (songDelay*1000) + (inlinePauseTotal*1000);
                        inlinePause.pauseEndTime = (songDelay*1000) + (thisTime*1000) + (inlinePauseTotal*1000);
                    } else {
                        // How long does it take to scroll this amount
                        scrollRequired = howFarDownSongHeight - displayHeight - prevScrollPos;
                        prevScrollPos = scrollRequired;
                        float numScrollActions = (float)scrollRequired/scrollIncrement;
                        int scrollTimeStart = Math.round((float)updateTime*numScrollActions);
                        inlinePause.pauseStartTime =  (songDelay*1000) + scrollTimeStart + (inlinePauseTotal*1000);
                        inlinePause.pauseEndTime = inlinePause.pauseStartTime + (thisTime*1000);
                    }
                    // Add this item
                    inlinePauses.add(inlinePause);
                    inlinePauseTotal = inlinePauseTotal + thisTime;
                }
            }
        }
        if (!inlinePauses.isEmpty()) {
            inlinePauseStartTime = inlinePauses.get(0).pauseStartTime;
            inlinePauseEndTime = inlinePauses.get(0).pauseEndTime;
            songDuration = songDuration + inlinePauseTotal;
        } else {
            inlinePauseStartTime = 0;
            inlinePauseEndTime = 0;
        }
    }

    private int stringToInt(String string) {
        if (string==null || string.isEmpty()) {
            return 0;
        } else {
            return Integer.parseInt(string);
        }
    }

}