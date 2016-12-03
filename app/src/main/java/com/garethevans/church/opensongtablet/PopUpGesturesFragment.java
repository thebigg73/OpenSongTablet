package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;

public class PopUpGesturesFragment extends DialogFragment {

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
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
        }
    }

    //Variables
    Spinner screenDoubleTap;
    Spinner screenLongPress;
    Spinner previousPedalLongPress;
    Spinner nextPedalLongPress;
    Spinner upPedalLongPress;
    Spinner downPedalLongPress;
    Button saveGestures;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getActivity().getResources().getString(R.string.customgestures));
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_gestures, container, false);

        // Initialise the views
        screenDoubleTap = (Spinner) V.findViewById(R.id.screenDoubleTap);
        screenLongPress = (Spinner) V.findViewById(R.id.screenLongPress);
        previousPedalLongPress = (Spinner) V.findViewById(R.id.previousPedalLongPress);
        nextPedalLongPress = (Spinner) V.findViewById(R.id.nextPedalLongPress);
        upPedalLongPress = (Spinner) V.findViewById(R.id.upPedalLongPress);
        downPedalLongPress = (Spinner) V.findViewById(R.id.downPedalLongPress);
        saveGestures = (Button) V.findViewById(R.id.saveGestures);

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
        ArrayAdapter<String> gestures = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, vals);
        gestures.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        screenDoubleTap.setAdapter(gestures);
        screenLongPress.setAdapter(gestures);
        previousPedalLongPress.setAdapter(gestures);
        nextPedalLongPress.setAdapter(gestures);
        upPedalLongPress.setAdapter(gestures);
        downPedalLongPress.setAdapter(gestures);

        // Set the current choices
        setSpinnerVal(screenDoubleTap,FullscreenActivity.gesture_doubletap);
        setSpinnerVal(screenLongPress,FullscreenActivity.gesture_longpress);
        setSpinnerVal(previousPedalLongPress,FullscreenActivity.longpresspreviouspedalgesture);
        setSpinnerVal(nextPedalLongPress,FullscreenActivity.longpressnextpedalgesture);
        setSpinnerVal(upPedalLongPress,FullscreenActivity.longpressuppedalgesture);
        setSpinnerVal(downPedalLongPress,FullscreenActivity.longpressdownpedalgesture);

        // Set the button listener
        saveGestures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int val_doubletap = screenDoubleTap.getSelectedItemPosition();
                int val_longpress = screenLongPress.getSelectedItemPosition();
                int val_prevpedallongpress = previousPedalLongPress.getSelectedItemPosition();
                int val_nextpedallongpress = nextPedalLongPress.getSelectedItemPosition();
                int val_uppedallongpress = upPedalLongPress.getSelectedItemPosition();
                int val_downpedallongpress = downPedalLongPress.getSelectedItemPosition();

                // if any of these values are -1, set them to 7
                if (val_doubletap<0) {
                    val_doubletap=7;
                }
                if (val_longpress<0) {
                    val_longpress=7;
                }
                if (val_prevpedallongpress<0) {
                    val_prevpedallongpress=7;
                }
                if (val_nextpedallongpress<0) {
                    val_nextpedallongpress=7;
                }
                if (val_uppedallongpress<0) {
                    val_uppedallongpress=7;
                }
                if (val_downpedallongpress<0) {
                    val_downpedallongpress=7;
                }

                FullscreenActivity.gesture_doubletap = "" + (val_doubletap+1);
                FullscreenActivity.gesture_longpress = "" + (val_longpress+1);
                FullscreenActivity.longpresspreviouspedalgesture = "" + (val_prevpedallongpress+1);
                FullscreenActivity.longpressnextpedalgesture = "" + (val_nextpedallongpress+1);
                FullscreenActivity.longpressuppedalgesture = "" + (val_uppedallongpress+1);
                FullscreenActivity.longpressdownpedalgesture = "" + (val_downpedallongpress+1);

                // Save the values
                Preferences.savePreferences();
                dismiss();
            }
        });
        return V;
    }

    public void setSpinnerVal (Spinner spinner, String val) {
        // Convert to integer
        int selection = 8;
        if (val!=null && !val.equals("")) {
            try {
                selection = Integer.parseInt(val);
                Log.d("d","selection="+selection);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (selection < 1 || selection > 8) {
                selection = 8;
            }
        }
        spinner.setSelection(selection-1);
    }
}