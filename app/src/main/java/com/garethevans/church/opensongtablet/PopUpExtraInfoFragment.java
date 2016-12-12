package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;

public class PopUpExtraInfoFragment extends DialogFragment {

    static PopUpExtraInfoFragment newInstance() {
        PopUpExtraInfoFragment frag;
        frag = new PopUpExtraInfoFragment();
        return frag;
    }

    public interface MyInterface {
        void refreshAll();
    }

    private MyInterface mListener;

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    SeekBar nextSong_seekBar;
    SeekBar stickyNotes_seekBar;
    Button closebutton;

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
        getDialog().setTitle(getActivity().getResources().getString(R.string.extra));
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_extrainfo, container, false);

        // Initialise the views
        nextSong_seekBar = (SeekBar) V.findViewById(R.id.nextSong_seekBar);
        stickyNotes_seekBar = (SeekBar) V.findViewById(R.id.stickyNotes_seekBar);
        closebutton = (Button) V.findViewById(R.id.closebutton);

        // Set the listeners
        nextSong_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (progress) {
                    case 0:
                        FullscreenActivity.showNextInSet = "off";
                        break;
                    case 1:
                        FullscreenActivity.showNextInSet = "bottom";
                        break;
                    case 2:
                        FullscreenActivity.showNextInSet = "top";
                        break;
                }
                Preferences.savePreferences();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        stickyNotes_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (progress) {
                    case 0:
                        FullscreenActivity.toggleAutoSticky = "N";
                        break;
                    case 1:
                        FullscreenActivity.toggleAutoSticky = "Y";
                        break;
                    case 2:
                        FullscreenActivity.toggleAutoSticky = "T";
                        break;
                    case 3:
                        FullscreenActivity.toggleAutoSticky = "B";
                        break;
                }
                Preferences.savePreferences();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        closebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                mListener.refreshAll();
            }
        });

        // Set the intial positions of the seekbars
        switch (FullscreenActivity.showNextInSet) {
            case "off":
                nextSong_seekBar.setProgress(0);
                break;
            case "bottom":
                nextSong_seekBar.setProgress(1);
                break;
            case "top":
                nextSong_seekBar.setProgress(2);
                break;
        }

        switch (FullscreenActivity.toggleAutoSticky) {
            case "N":
                stickyNotes_seekBar.setProgress(0);
                break;
            case "Y":
                stickyNotes_seekBar.setProgress(1);
                break;
            case "T":
                stickyNotes_seekBar.setProgress(2);
                break;
            case "B":
                stickyNotes_seekBar.setProgress(3);
                break;
        }
        return V;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}