/*
package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Objects;

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

    private SeekBar scalemenu_SeekBar;
    private TextView scalemenu_TextView;
    private TextView alphabeticalSize_TextView;
    private LinearLayout alphabeticalSizeGroup;
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
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_menusettings, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.menu_settings));
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
        alphabeticalSizeGroup = V.findViewById(R.id.alphabeticalSizeGroup);

        int pos = preferences.getMyPreferenceInt(getActivity(), "menuSize", 250);
        scale = pos + " px";
        pos = (int) (((float) pos -150)/50.0f);
        scalemenu_SeekBar.setProgress(pos);
        scalemenu_TextView.setText(scale);
        gesturesMenuSwipeButton.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"swipeForMenus",true));
        showSetTickBoxInSongMenu.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"songMenuSetTicksShow",true));
        showAlphabetInSongMenu.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"songMenuAlphaIndexShow",true));
        assignVisibility(alphabeticalSizeGroup, preferences.getMyPreferenceBoolean(getActivity(),"songMenuAlphaIndexShow",true));
        alphabeticalSize_SeekBar.setProgress(textSizeFloatToInt(preferences.getMyPreferenceFloat(getActivity(),"songMenuAlphaIndexSize",14.0f)));
        String s = ((int) preferences.getMyPreferenceFloat(getActivity(),"songMenuAlphaIndexSize",14.0f)) + "sp";
        alphabeticalSize_TextView.setText(s);

        // Set up the listeners
        scalemenu_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                i = (i*50) + 150;
                scale = i + "px";
                scalemenu_TextView.setText(scale);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int i = (scalemenu_SeekBar.getProgress()*50) + 150;
                preferences.setMyPreferenceInt(getActivity(),"menuSize",i);
                if (mListener!=null) {
                    mListener.resizeDrawers();
                }

            }
        });
        gesturesMenuSwipeButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                // IV - Was previous set to true - changed to set from the button
                preferences.setMyPreferenceBoolean(getActivity(),"swipeForMenus",b);
                if (mListener!=null) {
                    mListener.toggleDrawerSwipe();
                }
            }
        });
        showSetTickBoxInSongMenu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                preferences.setMyPreferenceBoolean(getActivity(),"songMenuSetTicksShow",b);
                if (mListener!=null) {
                    mListener.prepareSongMenu();
                }
            }
        });
        showAlphabetInSongMenu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                preferences.setMyPreferenceBoolean(getActivity(),"songMenuAlphaIndexShow",b);
                assignVisibility(alphabeticalSizeGroup,b);
                if (mListener!=null) {
                    mListener.prepareSongMenu();
                }
            }
        });
        alphabeticalSize_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // Text size is a float that adds 8 on to this
                i = i + 8;
                String s = i + "sp";
                alphabeticalSize_TextView.setText(s);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                preferences.setMyPreferenceFloat(getActivity(),"songMenuAlphaIndexSize",(float) seekBar.getProgress()+8);
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

    private void assignVisibility(View v, boolean b) {
        if (b) {
            v.setVisibility(View.VISIBLE);
        } else {
            v.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}*/
