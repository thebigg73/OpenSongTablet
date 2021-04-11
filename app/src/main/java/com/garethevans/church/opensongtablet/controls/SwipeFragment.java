package com.garethevans.church.opensongtablet.controls;

import android.content.Context;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.DrawNotes;
import com.garethevans.church.opensongtablet.databinding.SettingsSwipesBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;

import java.util.ArrayList;

public class SwipeFragment extends Fragment {

    private Preferences preferences;
    private MainActivityInterface mainActivityInterface;
    private SettingsSwipesBinding myView;
    private DrawNotes drawNotes;
    private Swipes swipes;

    // For simulated swipe animation
    private float startX, startY, newX, newY;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsSwipesBinding.inflate(inflater, container, false);
        mainActivityInterface.updateToolbar(null, getString(R.string.swipe));

        // setup the helpers
        setupHelpers();

        // register this fragement
        mainActivityInterface.registerFragment(this, "SwipeFragment");

        // setup the views
        new Thread(() -> requireActivity().runOnUiThread(this::setupViews)).start();

        return myView.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mainActivityInterface.registerFragment(null, "SwipeFragment");
    }

    private void setupHelpers() {
        preferences = mainActivityInterface.getPreferences();
        swipes = mainActivityInterface.getSwipes();
    }

    private void setupViews() {
        // The checkbox to enable/disable the settings
        myView.swipeActive.setChecked(preferences.getMyPreferenceBoolean(requireContext(), "swipeForSongs", true));
        myView.swipeActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                myView.swipeOptionsLayout.setVisibility(View.VISIBLE);
                simulateSwipe();
            } else {
                myView.swipeOptionsLayout.setVisibility(View.GONE);
            }
            preferences.setMyPreferenceBoolean(requireContext(), "swipeForSongs", isChecked);
        });

        // Set up the drawing area - attach the drawNotes to the desired view
        drawNotes = myView.drawingArea;
        drawNotes.setCurrentPaint(20,0xffffffff);
        drawNotes.delayClear = true;

        // Measure the view and set the sizes based on this and user preferences
        swipes.setSizes(drawNotes.getMeasuredWidth(), drawNotes.getMeasuredHeight());


        // Set up the seekBars
        setSeekBar(myView.swipeDistance, "swipeWidth", swipes.getWidthPx(), swipes.getMinWidth(), swipes.getMaxWidth(), true);
        setSeekBar(myView.swipeHeight, "swipeHeight", swipes.getHeightPx(), swipes.getMinHeight(), swipes.getMaxHeight(), true);
        setSeekBar(myView.swipeSpeed, "swipeTime", swipes.getTimeMs(), swipes.getMinTime(), swipes.getMaxTime(), true);
    }

    private void setSeekBar(SeekBar seekBar, String pref, int myval, int min, int max, boolean createListener) {
        // The seekBars have 10 positions (0-9)
        // How far up should we be?
        // position 0 -> min
        // position 9 -> max
        // divisions = max-min / 9
        float division = getProgressDivision(min, max);
        int prog = getProgressFromValue(myval, min, division);

        // Only create a new listener if we need to
        if (createListener) {
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (!fromUser) {
                        updatePreference(pref, (int) (progress * division) + min);
                        simulateSwipe();
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    updatePreference(pref, (int) (seekBar.getProgress() * division) + min);
                    simulateSwipe();
                }
            });
        }
        seekBar.setProgress(prog);
    }

    private float getProgressDivision(int min, int max) {
        // Seekbar is a max size of 10, so 9 jumps.
        // Get the difference of max-min and divide by 9
        return (float) ((max - min) / 9.0f);
    }

    private int getProgressFromValue(int myval, int min, float division) {
        // Because the seekbar position=0 will be worth a minimum value, remove this
        // Since we know what each position is worth the previous + division,
        // We can find out where our value lies in this range
        int progress = 0;
        for (int x=0; x<10; x++) {
            // Get the range of this position
            float thisMin = (int)(min+(x*division));
            float thisMax = (int)(min+((x+1)*division));
            if (x==0) {
                thisMin = 0;
            }
            if (myval>=thisMin && (myval<thisMax || x==9)) {
                progress = x;
            }
        }
        return progress;
    }

    private void updatePreference(String pref, int val) {
        // We've changed a preference.
        switch (pref) {
            case "swipeWidth":
                swipes.fixWidth(requireContext(), preferences, val);
                break;

            case "swipeHeight":
                swipes.fixHeight(requireContext(), preferences, val);
                break;

            case "swipeTime":
                swipes.fixTime(requireContext(), preferences, val);
                break;
        }
    }

    // Get the values back from the drawNotes vies via MainActivity
    public void getSwipeValues(int returnedWidth, int returnedHeight, int returnedTime) {
        // Change the seekbars to match
        setSeekBar(myView.swipeDistance, "swipeWidth", returnedWidth, swipes.getMinWidth(), swipes.getMaxWidth(), false);
        setSeekBar(myView.swipeHeight, "swipeHeight", returnedHeight, swipes.getMinHeight(), swipes.getMaxHeight(), false);
        setSeekBar(myView.swipeSpeed, "swipeTime", returnedTime, swipes.getMinTime(), swipes.getMaxTime(), false);
    }

    private boolean dealingWith = false;

    private void simulateSwipe() {
        // Only do one at a time
        if (!dealingWith) {
            dealingWith = true;
            startX = (drawNotes.getCanvasWidth() - swipes.getWidthPx()) / 2.0f;
            startY = (drawNotes.getCanvasHeight() + swipes.getHeightPx()) / 2.0f;

            // This will be drawn over the time chosen.
            // This will be called in a runnable
            // We will update every 50ms
            int timeBetween = 50;
            int updatesRequired = swipes.getTimeMs() / timeBetween;

            // How much should the width and height move by each time
            float moveByX = (float) ((float) swipes.getWidthPx() / (float) updatesRequired);
            float moveByY = (float) ((float) swipes.getHeightPx() / (float) updatesRequired);

            // Move to the correct start point
            drawNotes.resetSwipe();
            drawNotes.setSwipeAnimate(true);
            Path myPath = new Path();
            myPath.moveTo(startX, startY);
            drawNotes.addToSwipePaths(myPath);

            // Now build up a series of postDelayed handlers to build and draw the swipe gesture
            ArrayList<Handler> handlers = new ArrayList<>();
            ArrayList<Runnable> runnables = new ArrayList<>();

            for (int x = 0; x < updatesRequired; x++) {
                handlers.add(new Handler());
                runnables.add(() -> {
                    // Now go through each bit of the animation
                    newX = startX + moveByX;
                    newY = startY - moveByY;
                    myPath.quadTo(startX, startY, newX, newY);
                    drawNotes.addToSwipePaths(myPath);
                    startX = newX;
                    startY = newY;
                });
            }

            int timeNow = 0;
            // Now we have the handlers and runnables, set them into motion
            for (int x = 0; x < handlers.size(); x++) {
                handlers.get(x).postDelayed(runnables.get(x), timeNow + timeBetween);
                timeNow += timeBetween;
            }

            // Finally release the animation lock
            new Handler().postDelayed(() -> {
                drawNotes.setSwipeAnimate(false);
                drawNotes.resetSwipe();
                dealingWith = false;
            }, timeNow + timeBetween);

        }
    }
}
