package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

public class PopUpAutoScrollDefaultsFragment extends DialogFragment {

    static PopUpAutoScrollDefaultsFragment newInstance() {
        PopUpAutoScrollDefaultsFragment frag;
        frag = new PopUpAutoScrollDefaultsFragment();
        return frag;
    }

    private TextView default_delaytime_TextView;
    private SeekBar default_delaytime_SeekBar;
    private EditText default_delaymax_EditText;
    private EditText default_duration_EditText;
    private RadioButton autoscroll_default_RadioButton;
    private Preferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog()!=null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(true);
        }
        View V = inflater.inflate(R.layout.popup_autoscrolldefaults, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.default_autoscroll));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe, PopUpAutoScrollDefaultsFragment.this.getActivity());
            closeMe.setEnabled(false);
            PopUpAutoScrollDefaultsFragment.this.dismiss();
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe, PopUpAutoScrollDefaultsFragment.this.getActivity());
            saveMe.setEnabled(false);
            PopUpAutoScrollDefaultsFragment.this.doSave();
        });

        preferences = new Preferences();

        // Initialise the views
        default_delaytime_TextView = V.findViewById(R.id.default_delaytime_TextView);
        default_delaytime_SeekBar = V.findViewById(R.id.default_delaytime_SeekBar);
        default_delaymax_EditText = V.findViewById(R.id.default_delaymax_EditText);
        default_duration_EditText = V.findViewById(R.id.default_duration_EditText);
        autoscroll_default_RadioButton = V.findViewById(R.id.autoscroll_default_RadioButton);
        RadioButton autoscroll_prompt_RadioButton = V.findViewById(R.id.autoscroll_prompt_RadioButton);

        // Set them to the default values
        default_delaytime_SeekBar.setMax(preferences.getMyPreferenceInt(getActivity(),"autoscrollDefaultMaxPreDelay",30));
        default_delaytime_SeekBar.setProgress(preferences.getMyPreferenceInt(getActivity(),"autoscrollDefaultSongPreDelay",10));
        String text = preferences.getMyPreferenceInt(getActivity(),"autoscrollDefaultSongPreDelay",10) + " s";
        default_delaymax_EditText.setText(Integer.toString(preferences.getMyPreferenceInt(getActivity(),"autoscrollDefaultMaxPreDelay",30)));
        default_delaytime_TextView.setText(text);
        if (!preferences.getMyPreferenceBoolean(getActivity(),"autoscrollUseDefaultTime",false)) {
            autoscroll_prompt_RadioButton.setChecked(true);
            autoscroll_default_RadioButton.setChecked(false);
        } else {
            autoscroll_prompt_RadioButton.setChecked(false);
            autoscroll_default_RadioButton.setChecked(true);
        }

        text = "" + preferences.getMyPreferenceInt(getActivity(),"autoscrollDefaultSongLength",180);
        default_duration_EditText.setText(text);

        // Set listeners for changes and clicks
        default_delaytime_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    seekBar.setMax(Integer.parseInt(default_delaymax_EditText.getText().toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String text = progress + " s";
                default_delaytime_TextView.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    private void doSave(){
        int i;
        try {
            String s = default_delaymax_EditText.getText().toString();
            s = s.replaceAll("[^\\d.]", "");
            i = Integer.parseInt(s);
        } catch (Exception e) {
            i = 0;
        }
        preferences.setMyPreferenceInt(getActivity(),"autoscrollDefaultMaxPreDelay",i);
        preferences.setMyPreferenceInt(getActivity(),"autoscrollDefaultSongPreDelay",default_delaytime_SeekBar.getProgress());
        if (default_duration_EditText.getText()!=null) {
            try {
                int length = Integer.parseInt(default_duration_EditText.getText().toString());
                preferences.setMyPreferenceInt(getActivity(), "autoscrollDefaultSongLength", length);
            } catch (Exception e) {
                preferences.setMyPreferenceInt(getActivity(), "autoscrollDefaultSongLength", 180);
            }
        } else {
            preferences.setMyPreferenceInt(getActivity(),"autoscrollDefaultSongLength",180);
        }
        preferences.setMyPreferenceBoolean(getActivity(),"autoscrollUseDefaultTime",
                autoscroll_default_RadioButton.isChecked());

        dismiss();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        this.dismiss();
    }

}
