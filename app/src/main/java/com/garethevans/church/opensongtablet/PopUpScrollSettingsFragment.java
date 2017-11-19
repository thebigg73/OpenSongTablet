package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.TextView;

public class PopUpScrollSettingsFragment extends DialogFragment {

    public static Dialog setfrag;
    Activity a;
    String speed;
    String distance;
    SeekBar scrollspeed_SeekBar;
    SeekBar scrolldistance_SeekBar;
    TextView scrollspeed_TextView;
    TextView scrolldistance_TextView;

    static PopUpScrollSettingsFragment newInstance() {
        PopUpScrollSettingsFragment frag;
        frag = new PopUpScrollSettingsFragment();
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

        final View V = inflater.inflate(R.layout.popup_scrollsettings, container, false);
        setfrag = getDialog();

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.scrollbuttons));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setVisibility(View.GONE);

        if (getDialog().getWindow()!=null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Initialise the view
        scrollspeed_SeekBar = V.findViewById(R.id.scrollspeed_SeekBar);
        scrolldistance_SeekBar = V.findViewById(R.id.scrolldistance_SeekBar);
        scrollspeed_TextView = V.findViewById(R.id.scrollspeed_TextView);
        scrolldistance_TextView = V.findViewById(R.id.scrolldistance_TextView);

        // Set the current values
        setSpeed();
        setDistance();

        // Set the listeners
        scrollspeed_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                getSpeed(i);
                scrollspeed_TextView.setText(speed);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Preferences.savePreferences();
            }
        });
        scrolldistance_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                getDistance(i);
                scrolldistance_TextView.setText(distance);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Preferences.savePreferences();
            }
        });        return V;
    }

    public void getSpeed(int s) {
        // This will be between 0 and 15.  Add 5 and divide by 10
        // (min speed should be 0.5 secs and max should be 2.0)
        float val = ((float)s + 5.0f) / 10.0f;
        // Multiply this by 1000 to convert to milliseconds for the preferences
        FullscreenActivity.scrollSpeed = (int) (val * 1000);
        speed =  val + " s";
    }

    public void setSpeed() {
        // Take the preference and subtract 500 (minimum allowed is 500)
        int s = FullscreenActivity.scrollSpeed - 500;
        // Divide this by 100 to get a value between 0 and 15
        s = s/100;
        scrollspeed_SeekBar.setProgress(s);
        speed =  ((s+5) / 10.0f) + " s";
        scrollspeed_TextView.setText(speed);
    }

    public void getDistance(int d) {
        // This will be between 0 and 8
        // Add 2 on as minimum scroll amount is 20%
        d = d+2;
        FullscreenActivity.scrollDistance = ((float)d) / 10.0f;
        distance = (d*10) + " %";
    }

    public void setDistance() {
        // Take the preference and multiply by 10, then subtract 2
        // Pref can be between 0.2f and 1.0f, but we want an int betweeen 0 and 8
        distance = ((int)(FullscreenActivity.scrollDistance * 100.0f)) + " %";
        int d = ((int)(FullscreenActivity.scrollDistance * 10.0f)) - 2;
        scrolldistance_SeekBar.setProgress(d);
        scrolldistance_TextView.setText(distance);
    }

}
