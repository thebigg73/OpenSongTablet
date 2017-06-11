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
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_gestures, container, false);

        TextView title = (TextView) V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.customgestures));
        final FloatingActionButton closeMe = (FloatingActionButton) V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        final FloatingActionButton saveMe = (FloatingActionButton) V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(saveMe,getActivity());
                saveMe.setEnabled(false);
                doSave();
            }
        });

        // Initialise the views
        screenDoubleTap = (Spinner) V.findViewById(R.id.screenDoubleTap);
        screenLongPress = (Spinner) V.findViewById(R.id.screenLongPress);
        previousPedalLongPress = (Spinner) V.findViewById(R.id.previousPedalLongPress);
        nextPedalLongPress = (Spinner) V.findViewById(R.id.nextPedalLongPress);
        upPedalLongPress = (Spinner) V.findViewById(R.id.upPedalLongPress);
        downPedalLongPress = (Spinner) V.findViewById(R.id.downPedalLongPress);

        // Set up the spinner options
        ArrayList<String> vals = new ArrayList<>();
        vals.add(getString(R.string.off));
        vals.add(getString(R.string.gesture1));
        vals.add(getString(R.string.gesture2));
        vals.add(getString(R.string.gesture3));
        vals.add(getString(R.string.gesture4));
        vals.add(getString(R.string.autoscrollPedalText));
        vals.add(getString(R.string.padPedalText));
        vals.add(getString(R.string.metronomePedalText));
        vals.add(getString(R.string.autoscrollPedalText)+" + "+getString(R.string.padPedalText));
        vals.add(getString(R.string.autoscrollPedalText)+" + "+getString(R.string.metronomePedalText));
        vals.add(getString(R.string.padPedalText)+" + "+getString(R.string.metronomePedalText));
        vals.add(getString(R.string.autoscrollPedalText)+" + "+getString(R.string.padPedalText) + " + " + getString(R.string.metronomePedalText));

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

        // if any of these values are -1, set them to 0
        if (val_doubletap < 0) {
            val_doubletap = 0;
        }
        if (val_longpress < 0) {
            val_longpress = 0;
        }
        if (val_prevpedallongpress < 0) {
            val_prevpedallongpress = 0;
        }
        if (val_nextpedallongpress < 0) {
            val_nextpedallongpress = 0;
        }
        if (val_uppedallongpress < 0) {
            val_uppedallongpress = 0;
        }
        if (val_downpedallongpress < 0) {
            val_downpedallongpress = 0;
        }

        FullscreenActivity.gesture_doubletap = "" + (val_doubletap);
        FullscreenActivity.gesture_longpress = "" + (val_longpress);
        FullscreenActivity.longpresspreviouspedalgesture = "" + (val_prevpedallongpress);
        FullscreenActivity.longpressnextpedalgesture = "" + (val_nextpedallongpress);
        FullscreenActivity.longpressuppedalgesture = "" + (val_uppedallongpress);
        FullscreenActivity.longpressdownpedalgesture = "" + (val_downpedallongpress);

        // Save the values
        Preferences.savePreferences();
        dismiss();
    }

    public void setSpinnerVal(Spinner spinner, String val) {
        // Convert to integer
        int selection = 0;  // OFF is default for most
        if (val != null && !val.equals("")) {
            try {
                selection = Integer.parseInt(val);
                Log.d("d", "selection=" + selection);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (selection < 1 || selection > 12) {
                selection = 0;  // OFF
            }
        }
        spinner.setSelection(selection);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}