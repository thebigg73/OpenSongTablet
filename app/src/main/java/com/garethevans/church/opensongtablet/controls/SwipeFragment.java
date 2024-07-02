package com.garethevans.church.opensongtablet.controls;

import android.content.Context;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MaterialSlider;
import com.garethevans.church.opensongtablet.databinding.SettingsSwipesBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;

public class SwipeFragment extends Fragment {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "SwipeFragment";

    private MainActivityInterface mainActivityInterface;
    private SettingsSwipesBinding myView;
    private String webAddress;


    // For simulated swipe animation
    private float startX, startY, newX, newY;
    private String swipe_string="", website_swipe_settings_string="";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(swipe_string);
        mainActivityInterface.updateToolbarHelp(webAddress);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsSwipesBinding.inflate(inflater, container, false);

        prepareStrings();

        webAddress = website_swipe_settings_string;

        // register this fragement
        mainActivityInterface.registerFragment(this, "SwipeFragment");

        // setup the views
        new Thread(() -> {
            if (getActivity()!=null) {
                getActivity().runOnUiThread(this::setupViews);
            }
        }).start();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            swipe_string = getString(R.string.swipe);
            website_swipe_settings_string = getString(R.string.website_swipe_settings);
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mainActivityInterface.registerFragment(null, "SwipeFragment");
        myView = null;
    }

    private void setupViews() {
        // The checkbox to enable/disable the settings
        myView.swipeActive.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("swipeForSongs", true));
        showOrHide(myView.swipeActive.isChecked());

        myView.swipeActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                simulateSwipe();
            }
            showOrHide(isChecked);
            mainActivityInterface.getGestures().setPreferences("swipeForSongs", isChecked);
        });

        // Set up the drawing area - attach the drawNotes to the desired view
        mainActivityInterface.setDrawNotes(myView.drawingArea);
        mainActivityInterface.getDrawNotes().setCurrentPaint(20,0xffffffff);
        mainActivityInterface.getDrawNotes().delayClear = true;

        // Measure the view and set the sizes based on this and user preferences
        mainActivityInterface.getSwipes().setSizes(mainActivityInterface.getDrawNotes().getMeasuredWidth(), mainActivityInterface.getDrawNotes().getMeasuredHeight());

        // Set up the sliders
        setSlider(myView.swipeDistance, "swipeWidth", mainActivityInterface.getSwipes().getWidthPx(), mainActivityInterface.getSwipes().getMinWidth(), mainActivityInterface.getSwipes().getMaxWidth(), " px", true);
        setSlider(myView.swipeHeight, "swipeHeight", mainActivityInterface.getSwipes().getHeightPx(), mainActivityInterface.getSwipes().getMinHeight(), mainActivityInterface.getSwipes().getMaxHeight(), " px", true);
        setSlider(myView.swipeSpeed, "swipeTime", mainActivityInterface.getSwipes().getTimeMs(), mainActivityInterface.getSwipes().getMinTime(), mainActivityInterface.getSwipes().getMaxTime(), " ms", true);
    }

    private void showOrHide(boolean show) {
        if (show) {
            myView.swipeOptionsLayout.setVisibility(View.VISIBLE);
            myView.drawingArea.setVisibility(View.VISIBLE);
        } else {
            myView.drawingArea.setVisibility(View.GONE);
            myView.swipeOptionsLayout.setVisibility(View.GONE);
        }
    }
    private void setSlider(MaterialSlider slider, String pref, int myval, int min, int max, String unit, boolean createListener) {
        slider.setValueFrom(min);
        slider.setValueTo(max);
        slider.setStepSize(1.0f);
        if (myval<min) {
            myval = min;
        } else if (myval > max) {
            myval = max;
        }
        slider.setValue(myval);
        slider.setLabelFormatter(value -> ((int)value)+unit);
        slider.setHint(myval +unit);

        // Create listeners
        if (createListener) {
            slider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
                @Override
                public void onStartTrackingTouch(@NonNull Slider slider) {}

                @Override
                public void onStopTrackingTouch(@NonNull Slider slider) {
                    // Update the preference
                    float sliderVal = slider.getValue();
                    if (sliderVal < min) {
                        sliderVal = min;
                    } else if (sliderVal > max) {
                        sliderVal = max;
                    }
                    int newVal = Math.round(sliderVal);
                    switch (pref) {
                        case "swipeWidth":
                            mainActivityInterface.getSwipes().fixWidth(newVal);
                            break;

                        case "swipeHeight":
                            mainActivityInterface.getSwipes().fixHeight(newVal);
                            break;

                        case "swipeTime":
                            mainActivityInterface.getSwipes().fixTime(newVal);
                            break;
                    }
                    simulateSwipe();
                }
            });
            slider.addOnChangeListener((slider1, value, fromUser) -> {
                if (!fromUser) {
                    // Set by a drawing test
                    // Check we are in bounds
                    if (value < min) {
                        value = min;
                    } else if (value > max) {
                        value = max;
                    }
                    slider1.setValue(value);
                    simulateSwipe();
                }

                switch (pref) {
                    case "swipeTime":
                        myView.swipeSpeed.setHint(Math.round(value)+" ms");
                        break;
                    case "swipeWidth":
                        myView.swipeDistance.setHint(Math.round(value)+" px");
                        break;
                    case "swipeHeight":
                        myView.swipeHeight.setHint(Math.round(value)+" px");
                        break;
                }

            });
        }
    }


    // Get the values back from the drawNotes vies via MainActivity
    public void getSwipeValues(int returnedWidth, int returnedHeight, int returnedTime) {
        // Change the seekbars to match, but don't change the listeners
        setSlider(myView.swipeDistance, "swipeWidth", returnedWidth, mainActivityInterface.getSwipes().getMinWidth(), mainActivityInterface.getSwipes().getMaxWidth(), " px", false);
        setSlider(myView.swipeHeight, "swipeHeight", returnedHeight, mainActivityInterface.getSwipes().getMinHeight(), mainActivityInterface.getSwipes().getMaxHeight(), " px", false);
        setSlider(myView.swipeSpeed, "swipeTime", returnedTime, mainActivityInterface.getSwipes().getMinTime(), mainActivityInterface.getSwipes().getMaxTime(), " ms", false);
    }

    private boolean dealingWith = false;

    private void simulateSwipe() {
        // Only do one at a time
        if (!dealingWith) {
            dealingWith = true;
            startX = (mainActivityInterface.getDrawNotes().getCanvasWidth() - mainActivityInterface.getSwipes().getWidthPx()) / 2.0f;
            startY = (mainActivityInterface.getDrawNotes().getCanvasHeight() + mainActivityInterface.getSwipes().getHeightPx()) / 2.0f;

            // This will be drawn over the time chosen.
            // This will be called in a runnable
            // We will update every 50ms
            int timeBetween = 50;
            int updatesRequired = mainActivityInterface.getSwipes().getTimeMs() / timeBetween;

            // How much should the width and height move by each time
            float moveByX = (float) mainActivityInterface.getSwipes().getWidthPx() / (float) updatesRequired;
            float moveByY = (float) mainActivityInterface.getSwipes().getHeightPx() / (float) updatesRequired;

            // Move to the correct start point
            mainActivityInterface.getDrawNotes().resetSwipe();
            mainActivityInterface.getDrawNotes().setSwipeAnimate(true);
            Path myPath = new Path();
            myPath.moveTo(startX, startY);
            mainActivityInterface.getDrawNotes().addToSwipePaths(myPath);

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
                    mainActivityInterface.getDrawNotes().addToSwipePaths(myPath);
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
                mainActivityInterface.getDrawNotes().setSwipeAnimate(false);
                mainActivityInterface.getDrawNotes().resetSwipe();
                dealingWith = false;
            }, timeNow + timeBetween);

        }
    }
}
