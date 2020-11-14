package com.garethevans.church.opensongtablet;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

public class PopUpSwipeSettingsFragment extends DialogFragment {
    
    public interface MyInterface {
        void toggleDrawerSwipe();
    }

    private PopUpMenuSettingsFragment.MyInterface mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        mListener = (PopUpMenuSettingsFragment.MyInterface) context;
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    private String speed;
    private String distance;
    private String errordistance;
    private SeekBar swipedistance_SeekBar;
    private SeekBar swipespeed_SeekBar;
    private SeekBar swipeerror_SeekBar;
    private TextView swipedistance_TextView;
    private TextView swipespeed_TextView;
    private TextView swipeerror_TextView;
    private ImageView swipesimulateion_ImageView;
    private LinearLayout swipesettings;

    private Preferences preferences;

    static PopUpSwipeSettingsFragment newInstance() {
        PopUpSwipeSettingsFragment frag;
        frag = new PopUpSwipeSettingsFragment();
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (getDialog()!=null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(true);
        }

        final View V = inflater.inflate(R.layout.popup_swipesettings, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getResources().getString(R.string.swipe));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe,getActivity());
            closeMe.setEnabled(false);
            dismiss();
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        preferences = new Preferences();

        if (getDialog().getWindow()!=null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Initialise the view
        swipedistance_SeekBar = V.findViewById(R.id.swipedistance_SeekBar);
        swipespeed_SeekBar = V.findViewById(R.id.swipespeed_SeekBar);
        swipeerror_SeekBar = V.findViewById(R.id.swipeerror_SeekBar);
        swipedistance_TextView = V.findViewById(R.id.swipedistance_TextView);
        swipespeed_TextView = V.findViewById(R.id.swipespeed_TextView);
        swipeerror_TextView = V.findViewById(R.id.swipeerror_TextView);
        swipesimulateion_ImageView = V.findViewById(R.id.swipesimulateion_ImageView);
        swipesettings = V.findViewById(R.id.swipesettings);
        SwitchCompat gesturesSongSwipeButton = V.findViewById(R.id.gesturesSongSwipeButton);
        SwitchCompat gesturesMenuSwipeButton = V.findViewById(R.id.gesturesMenuSwipeButton);

        // Get the maximum values allowed for the seekbars
        // maxwidth is 80% of the screen width
        // Error is 400 pixels
        // Speed is 2000 pixels per second

        DisplayMetrics metrics = new DisplayMetrics();
        Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int maxheight = 400 - 100;
        int maxwidth = ((int) (0.8f * metrics.widthPixels)) - 100;
        int maxspeed = 2000;
        if (maxwidth <0) {
            maxwidth =0;
        }

        // If our max values are smaller than the stored ones, set the stored ones to match
        if (preferences.getMyPreferenceInt(getActivity(),"swipeMinimumDistance",250)> maxwidth) {
            preferences.getMyPreferenceInt(getActivity(),"swipeMinimumDistance", maxwidth);
        }
        if (preferences.getMyPreferenceInt(getActivity(),"swipeMaxDistanceYError",200)> maxheight) {
            preferences.getMyPreferenceInt(getActivity(),"swipeMaxDistanceYError", maxheight);
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
                swipeAnimate();
            }
        });

        gesturesMenuSwipeButton.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"swipeForMenus",true));

        gesturesMenuSwipeButton.setOnCheckedChangeListener((compoundButton, b) -> {
            preferences.setMyPreferenceBoolean(getActivity(),"swipeForMenus",b);
            if (mListener!=null) {
                mListener.toggleDrawerSwipe();
            }
        });

        gesturesSongSwipeButton.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"swipeForSongs",true));
        hideorunhideSettings();

        gesturesSongSwipeButton.setOnCheckedChangeListener((compoundButton, b) -> {
            preferences.setMyPreferenceBoolean(getActivity(),"swipeForSongs",b);
            hideorunhideSettings();
        });
        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);
        return V;
    }

    private void getSpeed(int s) {
        // This will be between 0 and 15.  Add 5 and divide by 10
        // (min speed should be 0.5 secs and max should be 2.0)
        int val = s+100;
        // Multiply this by 1000 to convert to milliseconds for the preferences
        preferences.setMyPreferenceInt(getActivity(),"swipeMinimumVelocity",val);
        speed =  val + " px/s";
    }

    private void setSpeed() {
        // Take the preference and subtract 100 (minimum allowed is 100)
        int s = preferences.getMyPreferenceInt(getActivity(),"swipeMinimumVelocity",600) - 100;
        if (s<0) {
            preferences.setMyPreferenceInt(getActivity(),"swipeMinimumVelocity",100);
            s=0;
        }
        swipespeed_SeekBar.setProgress(s);
        speed =  (s+100) + " px/s";
        swipespeed_TextView.setText(speed);
    }

    private void getErrorDistance(int d) {
        // Add 100 (minimum)
        d = d+100;
        preferences.setMyPreferenceInt(getActivity(),"swipeMaxDistanceYError",d);
        errordistance = d + " px";
    }

    private void setErrorDistance() {
        // Take the preference and subtract 100 (minimum allowed is 100)
        int d = preferences.getMyPreferenceInt(getActivity(),"swipeMaxDistanceYError",200) -100;
        if (d<0) {
            d=0;
            preferences.setMyPreferenceInt(getActivity(),"swipeMaxDistanceYError",100);
        }
        errordistance = (d+100) + " px";
        swipeerror_SeekBar.setProgress(d);
        swipeerror_TextView.setText(errordistance);
    }

    private void getDistance(int d) {
        // Add 100 (minimum)
        d = d+100;
        preferences.setMyPreferenceInt(getActivity(),"swipeMinimumDistance",d);
        distance = d + " px";
    }

    private void setDistance() {
        // Take the preference and subtract 100 (minimum allowed is 100)
        int d = preferences.getMyPreferenceInt(getActivity(),"swipeMinimumDistance",250) -100;
        if (d<0) {
            d=0;
            preferences.setMyPreferenceInt(getActivity(),"swipeMinimumDistance",100);
        }
        distance = (d+100) + " px";
        swipedistance_SeekBar.setProgress(d);
        swipedistance_TextView.setText(distance);
    }

    private void swipeAnimate() {
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(preferences.getMyPreferenceInt(getActivity(), "swipeMinimumDistance", 250),
                preferences.getMyPreferenceInt(getActivity(), "swipeMaxDistanceYError", 200));
        swipesimulateion_ImageView.setLayoutParams(llp);
        CustomAnimations.animateSwipe(swipesimulateion_ImageView,
                preferences.getMyPreferenceInt(getActivity(),"swipeMinimumDistance",250),
                preferences.getMyPreferenceInt(getActivity(),"swipeMinimumVelocity",600));
    }

    private void hideorunhideSettings() {
        if (preferences.getMyPreferenceBoolean(getActivity(),"swipeForSongs",true)) {
            swipesettings.setVisibility(View.VISIBLE);
        } else {
            swipesettings.setVisibility(View.GONE);
        }
    }
}
