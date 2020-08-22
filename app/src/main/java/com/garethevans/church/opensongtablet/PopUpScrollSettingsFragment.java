package com.garethevans.church.opensongtablet;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PopUpScrollSettingsFragment extends DialogFragment {

    private String speed;
    private String distance;
    private SeekBar scrollspeed_SeekBar;
    private SeekBar scrolldistance_SeekBar;
    private TextView scrollspeed_TextView;
    private TextView scrolldistance_TextView;

    private Preferences preferences;

    static PopUpScrollSettingsFragment newInstance() {
        PopUpScrollSettingsFragment frag;
        frag = new PopUpScrollSettingsFragment();
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
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        final View V = inflater.inflate(R.layout.popup_scrollsettings, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getString(R.string.scrollbuttons));
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
            public void onStopTrackingTouch(SeekBar seekBar) {}
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
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    private void getSpeed(int s) {
        // This will be between 0 and 15.  Add 5 and divide by 10
        // (min speed should be 0.5 secs and max should be 2.0)
        float val = ((float)s + 5.0f) / 10.0f;
        // Multiply this by 1000 to convert to milliseconds for the preferences
        preferences.setMyPreferenceInt(getActivity(),"scrollSpeed",(int) (val * 1000));
        speed =  val + " s";
    }

    private void setSpeed() {
        // Take the preference and subtract 500 (minimum allowed is 500)
        int s = preferences.getMyPreferenceInt(getActivity(),"scrollSpeed",1500) - 500;
        // Divide this by 100 to get a value between 0 and 15
        s = s/100;
        scrollspeed_SeekBar.setProgress(s);
        speed =  ((s+5) / 10.0f) + " s";
        scrollspeed_TextView.setText(speed);
    }

    private void getDistance(int d) {
        // This will be between 0 and 8
        // Add 2 on as minimum scroll amount is 20%
        d = d+2;
        preferences.setMyPreferenceFloat(getActivity(),"scrollDistance", ((float)d) / 10.0f);
        distance = (d*10) + " %";
    }

    private void setDistance() {
        // Take the preference and multiply by 10, then subtract 2
        // Pref can be between 0.2f and 1.0f, but we want an int betweeen 0 and 8
        distance = ((int)(preferences.getMyPreferenceFloat(getActivity(),"scrollDistance", 0.7f)* 100.0f)) + " %";
        int d = ((int)(preferences.getMyPreferenceFloat(getActivity(),"scrollDistance", 0.7f) * 10.0f)) - 2;
        scrolldistance_SeekBar.setProgress(d);
        scrolldistance_TextView.setText(distance);
    }

}
