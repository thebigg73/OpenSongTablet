package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class PopUpMenuSettingsFragment extends DialogFragment {

    static PopUpMenuSettingsFragment newInstance() {
        PopUpMenuSettingsFragment frag;
        frag = new PopUpMenuSettingsFragment();
        return frag;
    }

    public interface MyInterface {
        void toggleDrawerSwipe();
        void resizeDrawers();
        void prepareSongMenu();
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

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
        }
    }

    SeekBar scalemenu_SeekBar;
    TextView scalemenu_TextView;
    SwitchCompat gesturesMenuSwipeButton;
    SwitchCompat showSetTickBoxInSongMenu;
    int pos;
    String scale;

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
        View V = inflater.inflate(R.layout.popup_menusettings, container, false);

        TextView title = (TextView) V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.menu_settings));
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

        // Initialise the views
        scalemenu_SeekBar = (SeekBar) V.findViewById(R.id.scalemenu_SeekBar);
        scalemenu_TextView = (TextView) V.findViewById(R.id.scalemenu_TextView);
        gesturesMenuSwipeButton = (SwitchCompat) V.findViewById(R.id.gesturesMenuSwipeButton);
        showSetTickBoxInSongMenu = (SwitchCompat) V.findViewById(R.id.showSetTickBoxInSongMenu);

        pos = (int) (FullscreenActivity.menuSize * 10.0f) - 2;
        scale = (int) ((FullscreenActivity.menuSize * 100.0f)) + "%";
        scalemenu_SeekBar.setProgress(pos);
        scalemenu_TextView.setText(scale);
        gesturesMenuSwipeButton.setChecked(FullscreenActivity.swipeForMenus);
        showSetTickBoxInSongMenu.setChecked(FullscreenActivity.showSetTickBoxInSongMenu);

        // Set up the listeners
        scalemenu_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                FullscreenActivity.menuSize = (i+2) / 10.0f;
                scale = (int) ((FullscreenActivity.menuSize * 100.0f)) + "%";
                scalemenu_TextView.setText(scale);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Preferences.savePreferences();
                if (mListener!=null) {
                    mListener.resizeDrawers();
                }

            }
        });
        gesturesMenuSwipeButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.swipeForMenus = b;
                Preferences.savePreferences();
                if (mListener!=null) {
                    mListener.toggleDrawerSwipe();
                }
            }
        });
        showSetTickBoxInSongMenu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.showSetTickBoxInSongMenu = b;
                Preferences.savePreferences();
                if (mListener!=null) {
                    mListener.prepareSongMenu();
                }
            }
        });
        return V;
    }

    public void doSave() {
        FullscreenActivity.menuSize = (scalemenu_SeekBar.getProgress()+2)/10.0f;
        FullscreenActivity.swipeForMenus = gesturesMenuSwipeButton.isChecked();
        Preferences.savePreferences();
        if (mListener!=null) {
            mListener.resizeDrawers();
            mListener.toggleDrawerSwipe();
        }
        dismiss();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}