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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class PopUpPageButtonsFragment extends DialogFragment {

    static PopUpPageButtonsFragment newInstance() {
        PopUpPageButtonsFragment frag;
        frag = new PopUpPageButtonsFragment();
        return frag;
    }

    public interface MyInterface {
        void setupPageButtons(String s);
        void showpagebuttons();
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

    Button closeButton;
    SwitchCompat pageButtonSize_Switch;
    SeekBar pageButtonTransparency_seekBar;
    TextView transparency_TextView;

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
        getDialog().setTitle(getActivity().getResources().getString(R.string.pagebuttons));
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_pagebuttons, container, false);

        // Initialise the views
        closeButton = (Button) V.findViewById(R.id.closebutton);
        pageButtonSize_Switch = (SwitchCompat) V.findViewById(R.id.pageButtonSize_Switch);
        pageButtonTransparency_seekBar = (SeekBar) V.findViewById(R.id.pageButtonTransparency_seekBar);
        transparency_TextView = (TextView) V.findViewById(R.id.transparency_TextView);

        // Set the default values
        if (FullscreenActivity.fabSize == FloatingActionButton.SIZE_NORMAL) {
            pageButtonSize_Switch.setChecked(true);
        } else {
            pageButtonSize_Switch.setChecked(false);
        }
        int gettransp = (int) (FullscreenActivity.pageButtonAlpha * 100);
        String text = gettransp + "%";
        pageButtonTransparency_seekBar.setProgress(gettransp);
        transparency_TextView.setText(text);

        // Set the listeners
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        pageButtonSize_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    FullscreenActivity.fabSize = FloatingActionButton.SIZE_NORMAL;
                } else {
                    FullscreenActivity.fabSize = FloatingActionButton.SIZE_MINI;
                }
                Preferences.savePreferences();
                mListener.setupPageButtons("");
                mListener.showpagebuttons();
            }
        });
        pageButtonTransparency_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Transparency runs from 0% to 100%
                FullscreenActivity.pageButtonAlpha = progress / 100.0f;
                String text = progress + "%";
                transparency_TextView.setText(text);
                Preferences.savePreferences();
                mListener.setupPageButtons("");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        return V;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}
