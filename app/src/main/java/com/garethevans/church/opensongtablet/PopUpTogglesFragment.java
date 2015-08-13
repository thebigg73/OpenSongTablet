package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

public class PopUpTogglesFragment extends DialogFragment {

    static PopUpTogglesFragment newInstance() {
        PopUpTogglesFragment frag;
        frag = new PopUpTogglesFragment();
        return frag;
    }

    public interface MyInterface {
        void refreshAll();
    }

    private MyInterface mListener;

    @Override
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getActivity().getResources().getString(R.string.page_toggles));
        View V = inflater.inflate(R.layout.page_toggles, container, false);

        // Initialise the views
        Switch pageButtonsSwitch = (Switch) V.findViewById(R.id.pageButtonsSwitch);
        Switch menuSwipeSwitch = (Switch) V.findViewById(R.id.menuSwipeSwitch);
        Switch menuBarHideSwitch = (Switch) V.findViewById(R.id.menuBarHideSwitch);
        Switch songSwipeSwitch = (Switch) V.findViewById(R.id.songSwipeSwitch);
        Switch nextSongSwitch = (Switch) V.findViewById(R.id.nextSongSwitch);
        Switch showChordsSwitch = (Switch) V.findViewById(R.id.showChordsSwitch);

        // Set up listeners
        pageButtonsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    FullscreenActivity.togglePageButtons = "Y";
                } else {
                    FullscreenActivity.togglePageButtons = "N";
                }
                doSave();
            }
        });

        menuSwipeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    FullscreenActivity.swipeDrawer = "Y";
                } else {
                    FullscreenActivity.swipeDrawer = "N";
                }
                doSave();
            }
        });

        menuBarHideSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    FullscreenActivity.hideactionbaronoff = "Y";
                } else {
                    FullscreenActivity.hideactionbaronoff = "N";
                }
                doSave();
            }
        });

        songSwipeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    FullscreenActivity.hideactionbaronoff = "Y";
                } else {
                    FullscreenActivity.hideactionbaronoff = "N";
                }
                doSave();

            }
        });
        return V;
    }

    public void doSave() {
        Preferences.savePreferences();
        mListener.refreshAll();
    }
}
