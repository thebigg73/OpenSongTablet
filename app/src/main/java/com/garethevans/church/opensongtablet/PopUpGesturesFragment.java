package com.garethevans.church.opensongtablet;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class PopUpGesturesFragment extends DialogFragment {

    //Variables
    private Spinner screenDoubleTap;
    private Spinner screenLongPress;

    private Preferences preferences;

    static PopUpGesturesFragment newInstance() {
        PopUpGesturesFragment frag;
        frag = new PopUpGesturesFragment();
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
        if (getDialog()!=null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(true);
        }

        View V = inflater.inflate(R.layout.popup_gestures, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getString(R.string.customgestures));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe,getContext());
            closeMe.setEnabled(false);
            dismiss();
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(saveMe,getContext());
            saveMe.setEnabled(false);
            doSave();
        });

        preferences = new Preferences();

        // Initialise the views
        screenDoubleTap = V.findViewById(R.id.screenDoubleTap);
        screenLongPress = V.findViewById(R.id.screenLongPress);

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
        vals.add(requireContext().getString(R.string.currentset));

        // Set up the spinners
        ArrayAdapter<String> gestures = new ArrayAdapter<>(requireActivity(), R.layout.my_spinner, vals);
        gestures.setDropDownViewResource(R.layout.my_spinner);
        screenDoubleTap.setAdapter(gestures);
        screenLongPress.setAdapter(gestures);

        // Set the current choices
        setSpinnerVal(screenDoubleTap, preferences.getMyPreferenceInt(getContext(),"gestureScreenDoubleTap",2));
        setSpinnerVal(screenLongPress, preferences.getMyPreferenceInt(getContext(),"gestureScreenLongPress",0));

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    private void doSave() {
        int val_doubletap = screenDoubleTap.getSelectedItemPosition();
        int val_longpress = screenLongPress.getSelectedItemPosition();

        // if any of these values are -1, set them to 0
        if (val_doubletap < 0) {
            val_doubletap = 0;
        }
        if (val_longpress < 0) {
            val_longpress = 0;
        }

        preferences.setMyPreferenceInt(getContext(),"gestureScreenDoubleTap",val_doubletap);
        preferences.setMyPreferenceInt(getContext(),"gestureScreenLongPress",val_longpress);

        dismiss();
    }

    private void setSpinnerVal(Spinner spinner, int val) {
        spinner.setSelection(val);
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        this.dismiss();
    }

}