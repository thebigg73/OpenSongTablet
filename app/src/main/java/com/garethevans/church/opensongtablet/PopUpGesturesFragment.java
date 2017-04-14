package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class PopUpGesturesFragment extends DialogFragment {

    //Variables
    Spinner screenDoubleTap;
    Spinner screenLongPress;
    Spinner previousPedalLongPress;
    Spinner nextPedalLongPress;
    Spinner upPedalLongPress;
    Spinner downPedalLongPress;

    static PopUpGesturesFragment newInstance() {
        PopUpGesturesFragment frag;
        frag = new PopUpGesturesFragment();
        return frag;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog());
        }
        if (getDialog().getWindow() != null) {
            getDialog().getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.popup_dialogtitle);
            TextView title = (TextView) getDialog().getWindow().findViewById(R.id.dialogtitle);
            title.setText(getActivity().getResources().getString(R.string.customgestures));
            FloatingActionButton closeMe = (FloatingActionButton) getDialog().getWindow().findViewById(R.id.closeMe);
            closeMe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
            FloatingActionButton saveMe = (FloatingActionButton) getDialog().getWindow().findViewById(R.id.saveMe);
            saveMe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    doSave();
                }
            });
        } else {
            getDialog().setTitle(getActivity().getResources().getString(R.string.customgestures));
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
        getDialog().requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_gestures, container, false);

        // Initialise the views
        screenDoubleTap = (Spinner) V.findViewById(R.id.screenDoubleTap);
        screenLongPress = (Spinner) V.findViewById(R.id.screenLongPress);
        previousPedalLongPress = (Spinner) V.findViewById(R.id.previousPedalLongPress);
        nextPedalLongPress = (Spinner) V.findViewById(R.id.nextPedalLongPress);
        upPedalLongPress = (Spinner) V.findViewById(R.id.upPedalLongPress);
        downPedalLongPress = (Spinner) V.findViewById(R.id.downPedalLongPress);

        // Set up the spinner options
        ArrayList<String> vals = new ArrayList<>();
        vals.add(getString(R.string.gesture1));
        vals.add(getString(R.string.gesture2));
        vals.add(getString(R.string.gesture3));
        vals.add(getString(R.string.gesture4));
        vals.add(getString(R.string.autoscrollPedalText));
        vals.add(getString(R.string.padPedalText));
        vals.add(getString(R.string.metronomePedalText));
        vals.add(getString(R.string.off));

        // Set up the spinners
        ArrayAdapter<String> gestures = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, vals);
        gestures.setDropDownViewResource(R.layout.my_spinner);
        screenDoubleTap.setAdapter(gestures);
        screenLongPress.setAdapter(gestures);
        previousPedalLongPress.setAdapter(gestures);
        nextPedalLongPress.setAdapter(gestures);
        upPedalLongPress.setAdapter(gestures);
        downPedalLongPress.setAdapter(gestures);

        // Set the current choices
        setSpinnerVal(screenDoubleTap, FullscreenActivity.gesture_doubletap);
        setSpinnerVal(screenLongPress, FullscreenActivity.gesture_longpress);
        setSpinnerVal(previousPedalLongPress, FullscreenActivity.longpresspreviouspedalgesture);
        setSpinnerVal(nextPedalLongPress, FullscreenActivity.longpressnextpedalgesture);
        setSpinnerVal(upPedalLongPress, FullscreenActivity.longpressuppedalgesture);
        setSpinnerVal(downPedalLongPress, FullscreenActivity.longpressdownpedalgesture);

        return V;
    }

    public void doSave() {
        int val_doubletap = screenDoubleTap.getSelectedItemPosition();
        int val_longpress = screenLongPress.getSelectedItemPosition();
        int val_prevpedallongpress = previousPedalLongPress.getSelectedItemPosition();
        int val_nextpedallongpress = nextPedalLongPress.getSelectedItemPosition();
        int val_uppedallongpress = upPedalLongPress.getSelectedItemPosition();
        int val_downpedallongpress = downPedalLongPress.getSelectedItemPosition();

        // if any of these values are -1, set them to 7
        if (val_doubletap < 0) {
            val_doubletap = 7;
        }
        if (val_longpress < 0) {
            val_longpress = 7;
        }
        if (val_prevpedallongpress < 0) {
            val_prevpedallongpress = 7;
        }
        if (val_nextpedallongpress < 0) {
            val_nextpedallongpress = 7;
        }
        if (val_uppedallongpress < 0) {
            val_uppedallongpress = 7;
        }
        if (val_downpedallongpress < 0) {
            val_downpedallongpress = 7;
        }

        FullscreenActivity.gesture_doubletap = "" + (val_doubletap + 1);
        FullscreenActivity.gesture_longpress = "" + (val_longpress + 1);
        FullscreenActivity.longpresspreviouspedalgesture = "" + (val_prevpedallongpress + 1);
        FullscreenActivity.longpressnextpedalgesture = "" + (val_nextpedallongpress + 1);
        FullscreenActivity.longpressuppedalgesture = "" + (val_uppedallongpress + 1);
        FullscreenActivity.longpressdownpedalgesture = "" + (val_downpedallongpress + 1);

        // Save the values
        Preferences.savePreferences();
        dismiss();
    }

    public void setSpinnerVal(Spinner spinner, String val) {
        // Convert to integer
        int selection = 8;
        if (val != null && !val.equals("")) {
            try {
                selection = Integer.parseInt(val);
                Log.d("d", "selection=" + selection);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (selection < 1 || selection > 8) {
                selection = 8;
            }
        }
        spinner.setSelection(selection - 1);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}