package com.garethevans.church.opensongtablet;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Objects;

public class PopUpCrossFadeFragment extends DialogFragment {

    static PopUpCrossFadeFragment newInstance() {
        PopUpCrossFadeFragment frag;
        frag = new PopUpCrossFadeFragment();
        return frag;
    }

    Preferences preferences;

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
        View V = inflater.inflate(R.layout.popup_crossfadetime, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.crossfade_time));
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
        final SeekBar crossFadeSeekBar = V.findViewById(R.id.crossFadeSeekBar);
        crossFadeSeekBar.setMax(10);
        crossFadeSeekBar.setProgress((FullscreenActivity.crossFadeTime/1000)-2);
        final TextView crossFadeText = V.findViewById(R.id.crossFadeText);
        String newtext = (FullscreenActivity.crossFadeTime/1000)+ " s";
        crossFadeText.setText(newtext);

        // Set Listeners
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
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}
