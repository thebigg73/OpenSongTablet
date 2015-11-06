package com.garethevans.church.opensongtablet;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class PopUpCrossFadeFragment extends DialogFragment {

    static PopUpCrossFadeFragment newInstance() {
        PopUpCrossFadeFragment frag;
        frag = new PopUpCrossFadeFragment();
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getActivity().getResources().getString(R.string.crossfade_time));
        View V = inflater.inflate(R.layout.popup_crossfadetime, container, false);

        // Initialise the views
        final SeekBar crossFadeSeekBar = (SeekBar) V.findViewById(R.id.crossFadeSeekBar);
        crossFadeSeekBar.setMax(10);
        crossFadeSeekBar.setProgress((FullscreenActivity.crossFadeTime/1000)-2);
        final TextView crossFadeText = (TextView) V.findViewById(R.id.crossFadeText);
        String newtext = (FullscreenActivity.crossFadeTime/1000)+ " s";
        crossFadeText.setText(newtext);
        Button crossFadeClose = (Button) V.findViewById(R.id.crossFadeClose);

        // Set Listeners
        crossFadeClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        crossFadeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                FullscreenActivity.crossFadeTime = (progress + 2) * 1000;
                String newtext = (progress+2)+ "s";
                crossFadeText.setText(newtext);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Preferences.savePreferences();
            }
        });
        return V;
    }
}
