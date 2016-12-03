package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class PopUpAutoscrollFragment extends DialogFragment {

    static PopUpAutoscrollFragment newInstance() {
        PopUpAutoscrollFragment frag;
        frag = new PopUpAutoscrollFragment();
        return frag;
    }

    public interface MyInterface {
        void pageButtonAlpha(String s);
        void loadSong();
        void startAutoScroll();
        void stopAutoScroll();
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
            PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog());
        }
    }

    Button save_autoscroll_Button;
    Button popupautoscroll_startstopbutton;
    SeekBar popupautoscroll_delay;
    TextView popupautoscroll_delay_text;
    EditText popupautoscroll_duration;
    ImageButton uselinkaudiolength_ImageButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getActivity().getResources().getString(R.string.autoscroll));
        getDialog().setCanceledOnTouchOutside(true);
        mListener.pageButtonAlpha("autoscroll");

        View V = inflater.inflate(R.layout.popup_page_autoscroll, container, false);

        // Initialise the views
        save_autoscroll_Button = (Button) V.findViewById(R.id.save_autoscroll_Button);
        popupautoscroll_startstopbutton = (Button) V.findViewById(R.id.popupautoscroll_startstopbutton);
        popupautoscroll_delay = (SeekBar) V.findViewById(R.id.popupautoscroll_delay);
        popupautoscroll_delay_text = (TextView) V.findViewById(R.id.popupautoscroll_delay_text);
        popupautoscroll_duration = (EditText) V.findViewById(R.id.popupautoscroll_duration);
        uselinkaudiolength_ImageButton = (ImageButton) V.findViewById(R.id.uselinkaudiolength_ImageButton);

        // Set up current values
        AutoScrollFunctions.getAutoScrollTimes();
        String text;
        if (FullscreenActivity.autoScrollDelay < 0) {
            popupautoscroll_delay.setProgress(0);
            text = "";
        } else {
            popupautoscroll_delay.setProgress(FullscreenActivity.autoScrollDelay + 1);
            text = FullscreenActivity.autoScrollDelay + " s";
        }
        popupautoscroll_delay_text.setText(text);
        Log.d("d","mDuration="+FullscreenActivity.mDuration);
        Log.d("d","autoScrollDuration="+FullscreenActivity.autoScrollDuration);

        String text2 = FullscreenActivity.autoScrollDuration + "";
        if (FullscreenActivity.autoScrollDuration>-1) {
            popupautoscroll_duration.setText(text2);
        } else {
            popupautoscroll_duration.setText("");
        }

        // Set up the listeners
        save_autoscroll_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doSave();
            }
        });
        popupautoscroll_delay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                FullscreenActivity.mPreDelay = ""+i;
                String s = i + " s";
                popupautoscroll_delay_text.setText(s);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        uselinkaudiolength_ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                grabLinkAudioTime();
            }
        });
        if (FullscreenActivity.isautoscrolling) {
            popupautoscroll_startstopbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.stopAutoScroll();
                    dismiss();
                }
            });
            popupautoscroll_startstopbutton.setText(getActivity().getResources().getString(R.string.stop));

        } else {
            popupautoscroll_startstopbutton.setText(getActivity().getResources().getString(R.string.start));
            popupautoscroll_startstopbutton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    mListener.startAutoScroll();
                    dismiss();
                }
            });
        }

        return V;
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        if (mListener!=null) {
            mListener.pageButtonAlpha("");
        }
    }

    public void doSave() {
        try {
            FullscreenActivity.mPreDelay = popupautoscroll_delay.getProgress()+"";
            FullscreenActivity.mDuration = popupautoscroll_duration.getText().toString();
            FullscreenActivity.autoScrollDuration = Integer.parseInt(popupautoscroll_duration.getText().toString());
            PopUpEditSongFragment.prepareSongXML();
            PopUpEditSongFragment.justSaveSongXML();
            FullscreenActivity.myToastMessage = getActivity().getResources().getString(R.string.savesong) + " - " +
                    getActivity().getResources().getString(R.string.ok);
            ShowToast.showToast(getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void grabLinkAudioTime() {
        AutoScrollFunctions.getAudioLength(getActivity());
        if (FullscreenActivity.audiolength>-1) {
            // If this is a valid audio length, set the mDuration value
            FullscreenActivity.mDuration = "" + FullscreenActivity.audiolength;
            popupautoscroll_duration.setText(FullscreenActivity.mDuration);
        } else {
            FullscreenActivity.myToastMessage = getActivity().getResources().getString(R.string.link_audio) + " - " +
                    getActivity().getResources().getString(R.string.notset);
            ShowToast.showToast(getActivity());
        }
    }
}
