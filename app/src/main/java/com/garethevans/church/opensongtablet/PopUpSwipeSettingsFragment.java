package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.SwitchCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class PopUpSwipeSettingsFragment extends DialogFragment {

    public static Dialog setfrag;
    Activity a;
    String speed;
    String distance;
    String errordistance;
    SeekBar swipedistance_SeekBar;
    SeekBar swipespeed_SeekBar;
    SeekBar swipeerror_SeekBar;
    TextView swipedistance_TextView;
    TextView swipespeed_TextView;
    TextView swipeerror_TextView;
    ImageView swipesimulateion_ImageView;
    LinearLayout swipesettings;
    SwitchCompat gesturesSongSwipeButton;

    int maxwidth;
    int maxheight;
    int maxspeed;
    LinearLayout.LayoutParams llp;

    static PopUpSwipeSettingsFragment newInstance() {
        PopUpSwipeSettingsFragment frag;
        frag = new PopUpSwipeSettingsFragment();
        return frag;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        a = getActivity();
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        final View V = inflater.inflate(R.layout.popup_swipesettings, container, false);
        setfrag = getDialog();

        TextView title = (TextView) V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.swipe));
        final FloatingActionButton closeMe = (FloatingActionButton) V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        FloatingActionButton saveMe = (FloatingActionButton) V.findViewById(R.id.saveMe);
        saveMe.setVisibility(View.GONE);

        if (getDialog().getWindow()!=null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Initialise the view
        swipedistance_SeekBar = (SeekBar) V.findViewById(R.id.swipedistance_SeekBar);
        swipespeed_SeekBar = (SeekBar) V.findViewById(R.id.swipespeed_SeekBar);
        swipeerror_SeekBar = (SeekBar) V.findViewById(R.id.swipeerror_SeekBar);
        swipedistance_TextView = (TextView) V.findViewById(R.id.swipedistance_TextView);
        swipespeed_TextView = (TextView) V.findViewById(R.id.swipespeed_TextView);
        swipeerror_TextView = (TextView) V.findViewById(R.id.swipeerror_TextView);
        swipesimulateion_ImageView = (ImageView) V.findViewById(R.id.swipesimulateion_ImageView);
        swipesettings = (LinearLayout) V.findViewById(R.id.swipesettings);
        gesturesSongSwipeButton = (SwitchCompat) V.findViewById(R.id.gesturesSongSwipeButton);

        // Get the maximum values allowed for the seekbars
        // maxwidth is 80% of the screen width
        // Error is 400 pixels
        // Speed is 2000 pixels per second

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        maxheight = 400 - 100;
        maxwidth = ((int) (0.8f * metrics.widthPixels)) - 100;
        maxspeed = 2000;
        if (maxwidth<0) {
            maxwidth=0;
        }

        // If our max values are smaller than the stored ones, set the stored ones to match
        if (FullscreenActivity.SWIPE_MIN_DISTANCE>maxwidth) {
            FullscreenActivity.SWIPE_MIN_DISTANCE = maxwidth;
        }
        if (FullscreenActivity.SWIPE_MAX_OFF_PATH>maxheight) {
            FullscreenActivity.SWIPE_MAX_OFF_PATH = maxheight;
        }

        // Set the SeekBar sizes
        swipedistance_SeekBar.setMax(maxwidth);
        swipeerror_SeekBar.setMax(maxheight);
        swipespeed_SeekBar.setMax(maxspeed - 100);

        // Set the swipeanimation
        swipeAnimate();

        // Set the current values
        setSpeed();
        setDistance();
        setErrorDistance();

        // Set the listeners
        swipespeed_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                getSpeed(i);
                swipespeed_TextView.setText(speed);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Preferences.savePreferences();
                swipeAnimate();
            }
        });
        swipedistance_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                getDistance(i);
                swipedistance_TextView.setText(distance);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Preferences.savePreferences();
                swipeAnimate();
            }
        });
        swipeerror_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                getErrorDistance(i);
                swipeerror_TextView.setText(errordistance);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Preferences.savePreferences();
                swipeAnimate();
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            gesturesSongSwipeButton.setChecked(FullscreenActivity.swipeForSongs);
        }
        hideorunhideSettings();

        gesturesSongSwipeButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.swipeForSongs = b;
                Preferences.savePreferences();
                hideorunhideSettings();
            }
        });
        return V;
    }

    public void getSpeed(int s) {
        // This will be between 0 and 15.  Add 5 and divide by 10
        // (min speed should be 0.5 secs and max should be 2.0)
        int val = s+100;
        // Multiply this by 1000 to convert to milliseconds for the preferences
        FullscreenActivity.SWIPE_THRESHOLD_VELOCITY = val;
        speed =  val + " px/s";
    }

    public void setSpeed() {
        // Take the preference and subtract 100 (minimum allowed is 100)
        int s = FullscreenActivity.SWIPE_THRESHOLD_VELOCITY - 100;
        if (s<0) {
            FullscreenActivity.SWIPE_THRESHOLD_VELOCITY = 100;
            s=0;
        }
        swipespeed_SeekBar.setProgress(s);
        speed =  (s+100) + " px/s";
        swipespeed_TextView.setText(speed);
    }

    public void getErrorDistance(int d) {
        // Add 100 (minimum)
        d = d+100;
        FullscreenActivity.SWIPE_MAX_OFF_PATH = d;
        errordistance = d + " px";
    }

    public void setErrorDistance() {
        // Take the preference and subtract 100 (minimum allowed is 100)
        int d = FullscreenActivity.SWIPE_MAX_OFF_PATH -100;
        if (d<0) {
            d=0;
            FullscreenActivity.SWIPE_MAX_OFF_PATH = 100;
        }
        errordistance = d + " px";
        swipeerror_SeekBar.setProgress(d);
        swipeerror_TextView.setText(errordistance);
    }

    public void getDistance(int d) {
        // Add 100 (minimum)
        d = d+100;
        FullscreenActivity.SWIPE_MIN_DISTANCE = d;
        distance = d + " px";
    }

    public void setDistance() {
        // Take the preference and subtract 100 (minimum allowed is 100)
        int d = FullscreenActivity.SWIPE_MIN_DISTANCE -100;
        if (d<0) {
            d=0;
            FullscreenActivity.SWIPE_MIN_DISTANCE = 100;
        }
        distance = d + " px";
        swipedistance_SeekBar.setProgress(d);
        swipedistance_TextView.setText(distance);
    }

    public void swipeAnimate() {
        llp = new LinearLayout.LayoutParams(FullscreenActivity.SWIPE_MIN_DISTANCE,FullscreenActivity.SWIPE_MAX_OFF_PATH);
        swipesimulateion_ImageView.setLayoutParams(llp);
        CustomAnimations.animateSwipe(swipesimulateion_ImageView);
    }

    public void hideorunhideSettings() {
        if (FullscreenActivity.swipeForSongs) {
            swipesettings.setVisibility(View.VISIBLE);
        } else {
            swipesettings.setVisibility(View.GONE);
        }
    }
}
