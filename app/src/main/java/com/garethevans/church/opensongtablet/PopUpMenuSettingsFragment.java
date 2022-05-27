package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
    public void onAttach(@NonNull Context context) {
        mListener = (MyInterface) context;
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    private SeekBar scalemenu_SeekBar;
    private TextView scalemenu_TextView;
    private TextView alphabeticalSize_TextView;
    private String scale;
    private Preferences preferences;

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
        View V = inflater.inflate(R.layout.popup_menusettings, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getString(R.string.menu_settings));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe,getContext());
            closeMe.setEnabled(false);
            dismiss();
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        preferences = new Preferences();

        // Initialise the views
        scalemenu_SeekBar = V.findViewById(R.id.scalemenu_SeekBar);
        scalemenu_TextView = V.findViewById(R.id.scalemenu_TextView);
        SwitchCompat gesturesMenuSwipeButton = V.findViewById(R.id.gesturesMenuSwipeButton);
        SwitchCompat showSetTickBoxInSongMenu = V.findViewById(R.id.showSetTickBoxInSongMenu);
        SwitchCompat showAlphabetInSongMenu = V.findViewById(R.id.showAlphabetInSongMenu);
        SeekBar alphabeticalSize_SeekBar = V.findViewById(R.id.alphabeticalSize_SeekBar);
        alphabeticalSize_TextView = V.findViewById(R.id.alphabeticalSize_TextView);

        int pos = preferences.getMyPreferenceInt(getContext(), "menuSize", 250);
        scale = pos + " px";
        pos = (int) (((float) pos -150)/50.0f);
        scalemenu_SeekBar.setProgress(pos);
        scalemenu_TextView.setText(scale);
        gesturesMenuSwipeButton.setChecked(preferences.getMyPreferenceBoolean(getContext(),"swipeForMenus",true));
        showSetTickBoxInSongMenu.setChecked(preferences.getMyPreferenceBoolean(getContext(),"songMenuSetTicksShow",true));
        showAlphabetInSongMenu.setChecked(preferences.getMyPreferenceBoolean(getContext(),"songMenuAlphaIndexShow",true));
        alphabeticalSize_SeekBar.setProgress(textSizeFloatToInt(preferences.getMyPreferenceFloat(getContext(),"songMenuAlphaIndexSize",14.0f)));
        String s = ((int) preferences.getMyPreferenceFloat(getContext(),"songMenuAlphaIndexSize",14.0f)) + " sp";
        alphabeticalSize_TextView.setText(s);

        // Set up the listeners
        scalemenu_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                i = (i*50) + 150;
                scale = i + " px";
                scalemenu_TextView.setText(scale);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int i = (scalemenu_SeekBar.getProgress()*50) + 150;
                preferences.setMyPreferenceInt(getContext(),"menuSize",i);
                if (mListener!=null) {
                    mListener.resizeDrawers();
                }

            }
        });
        gesturesMenuSwipeButton.setOnCheckedChangeListener((compoundButton, b) -> {
            // IV - Was previous set to true - changed to set from the button
            preferences.setMyPreferenceBoolean(getContext(),"swipeForMenus",b);
            if (mListener!=null) {
                mListener.toggleDrawerSwipe();
            }
        });
        showSetTickBoxInSongMenu.setOnCheckedChangeListener((compoundButton, b) -> {
            preferences.setMyPreferenceBoolean(getContext(),"songMenuSetTicksShow",b);
            if (mListener!=null) {
                mListener.prepareSongMenu();
            }
        });
        showAlphabetInSongMenu.setOnCheckedChangeListener((compoundButton, b) -> {
            preferences.setMyPreferenceBoolean(getContext(),"songMenuAlphaIndexShow",b);
            if (mListener!=null) {
                mListener.prepareSongMenu();
            }
        });
        alphabeticalSize_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // Text size is a float that adds 8 on to this
                i = i + 8;
                String s = i + " sp";
                alphabeticalSize_TextView.setText(s);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                preferences.setMyPreferenceFloat(getContext(),"songMenuAlphaIndexSize",(float) seekBar.getProgress()+8);
                if (mListener!=null) {
                    mListener.prepareSongMenu();
                }
            }
        });

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    private int textSizeFloatToInt(float f) {
        // Minimum text size is 8sp (float).  This should match 0 on the Seekbar
        int size = (int) f;
        if (size>=8) {
            size = size - 8;
        } else {
            size = 0;
        }
        return size;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        this.dismiss();
    }

}