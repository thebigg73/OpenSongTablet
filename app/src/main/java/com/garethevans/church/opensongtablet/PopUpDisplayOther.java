package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

public class PopUpDisplayOther extends DialogFragment {

    static PopUpDisplayOther newInstance() {
        PopUpDisplayOther frag;
        frag = new PopUpDisplayOther();
        return frag;
    }

    public interface MyInterface {
        void resizeDrawers();
        void refreshAll();
        void openFragment();
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

    private SwitchCompat hideActionBar_Switch, setCheckBox_Switch, alphabeticalList_Switch;
    private Button extraInformation_Button, popupSettings_Button, actionBarSettings_Button;
    private SeekBar menuWidth_SeekBar, menuTextSize_SeekBar;
    private TextView menuWidth_TextView, menuTextSize_TextView;
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
        View V = inflater.inflate(R.layout.popup_displayother, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.extra));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe, getActivity());
                closeMe.setEnabled(false);
                mListener.refreshAll();
                dismiss();
            }
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        preferences = new Preferences();

        // Initialise the views
        initialiseViews(V);

        // Set the buttons and seek bars
        setListeners();

        PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog(), preferences);

        return V;
    }

    private void initialiseViews(View V) {
        hideActionBar_Switch = V.findViewById(R.id.hideActionBar_Switch);
        setCheckBox_Switch = V.findViewById(R.id.setCheckBox_Switch);
        alphabeticalList_Switch = V.findViewById(R.id.alphabeticalList_Switch);
        extraInformation_Button = V.findViewById(R.id.extraInformation_Button);
        popupSettings_Button = V.findViewById(R.id.popupSettings_Button);
        actionBarSettings_Button = V.findViewById(R.id.actionBarSettings_Button);
        menuWidth_SeekBar = V.findViewById(R.id.menuWidth_SeekBar);
        menuTextSize_SeekBar = V.findViewById(R.id.menuTextSize_SeekBar);
        menuWidth_TextView = V.findViewById(R.id.menuWidth_TextView);
        menuTextSize_TextView = V.findViewById(R.id.menuTextSize_TextView);
    }

    private void setListeners() {
        extraInformation_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener!=null) {
                    FullscreenActivity.whattodo = "extra";
                    mListener.openFragment();
                }
                try {
                    dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        popupSettings_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener!=null) {
                    FullscreenActivity.whattodo = "popupsettings";
                    mListener.openFragment();
                }
                try {
                    dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        actionBarSettings_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener!=null) {
                    FullscreenActivity.whattodo = "actionbarinfo";
                    mListener.openFragment();
                }
                try {
                    dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        hideActionBar_Switch.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"hideActionBar",false));
        setCheckBox_Switch.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"songMenuSetTicksShow",true));
        alphabeticalList_Switch.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"songMenuAlphaIndexShow",true));
        int width = preferences.getMyPreferenceInt(getActivity(), "menuSize", 150);
        menuWidth_SeekBar.setProgress((width-150)/50);
        String text = width + "px";
        menuWidth_TextView.setText(text);
        menuWidth_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = (progress*50) + 150;
                String scale = progress + "px";
                menuWidth_TextView.setText(scale);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int i = (seekBar.getProgress()*50) + 150;
                preferences.setMyPreferenceInt(getActivity(),"menuSize",i);
                if (mListener!=null) {
                    mListener.resizeDrawers();
                }
            }
        });
        float textSize = preferences.getMyPreferenceFloat(getActivity(),"songMenuAlphaIndexSize",14.0f);
        menuTextSize_SeekBar.setProgress((int)(textSize-14.0f)-8);
        text = textSize + "sp";
        menuTextSize_TextView.setText(text);
        menuTextSize_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = progress + 8;
                String text = progress + "sp";
                menuTextSize_TextView.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                progress = progress + 8;
                preferences.setMyPreferenceFloat(getActivity(),"songMenuAlphaIndexSize", (float)progress);
                if (mListener!=null) {
                    mListener.prepareSongMenu();
                }
            }
        });

    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
        mListener.refreshAll();
    }
}

