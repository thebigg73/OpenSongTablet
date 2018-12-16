package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
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
        void prepareLearnAutoScroll();
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

    Button popupautoscroll_startstopbutton;
    Button popupautoscroll_learnbutton;
    SeekBar popupautoscroll_delay;
    TextView popupautoscroll_delay_text;
    EditText popupautoscroll_duration;
    FloatingActionButton uselinkaudiolength_ImageButton;

    boolean mStopHandler = false;
    Handler mHandler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                checkAutoScrollStatus();
            } catch (Exception e) {
                mStopHandler = true;
            }
            if (!mStopHandler) {
                mHandler.postDelayed(this, 2000);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.dismiss();
        }
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        mListener.pageButtonAlpha("autoscroll");

        View V = inflater.inflate(R.layout.popup_page_autoscroll, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.autoscroll));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe, PopUpAutoscrollFragment.this.getActivity());
                closeMe.setEnabled(false);
                PopUpAutoscrollFragment.this.doSave();
            }
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setVisibility(View.GONE);

        // Initialise the views
        popupautoscroll_startstopbutton = V.findViewById(R.id.popupautoscroll_startstopbutton);
        popupautoscroll_learnbutton = V.findViewById(R.id.popupautoscroll_learnbutton);
        popupautoscroll_delay = V.findViewById(R.id.popupautoscroll_delay);
        popupautoscroll_delay_text = V.findViewById(R.id.popupautoscroll_delay_text);
        popupautoscroll_duration = V.findViewById(R.id.popupautoscroll_duration);
        uselinkaudiolength_ImageButton = V.findViewById(R.id.uselinkaudiolength_ImageButton);
        popupautoscroll_delay.setMax(FullscreenActivity.default_autoscroll_predelay_max);
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

        String text2 = FullscreenActivity.autoScrollDuration + "";
        if (FullscreenActivity.autoScrollDuration>-1) {
            popupautoscroll_duration.setText(text2);
        } else {
            popupautoscroll_duration.setText("");
        }

        // Set up the listeners
        popupautoscroll_learnbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener!=null) {
                    try {
                        mListener.prepareLearnAutoScroll();
                        dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        popupautoscroll_duration.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                FullscreenActivity.mDuration = textView.getText().toString();
                FullscreenActivity.autoscrollok = ProcessSong.isAutoScrollValid();
                Preferences.savePreferences();
                return false;
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
            public void onStopTrackingTouch(SeekBar seekBar) {
                FullscreenActivity.autoscrollok = ProcessSong.isAutoScrollValid();
                Preferences.savePreferences();
            }
        });
        uselinkaudiolength_ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(uselinkaudiolength_ImageButton, PopUpAutoscrollFragment.this.getActivity());
                PopUpAutoscrollFragment.this.grabLinkAudioTime();
                FullscreenActivity.autoscrollok = ProcessSong.isAutoScrollValid();
                Preferences.savePreferences();
            }
        });
        if (FullscreenActivity.isautoscrolling) {
            popupautoscroll_startstopbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FullscreenActivity.clickedOnAutoScrollStart = false;
                    mListener.stopAutoScroll();
                    PopUpAutoscrollFragment.this.dismiss();
                }
            });
            popupautoscroll_startstopbutton.setText(getActivity().getResources().getString(R.string.stop));

        } else {
            popupautoscroll_startstopbutton.setText(getActivity().getResources().getString(R.string.start));
            popupautoscroll_startstopbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (FullscreenActivity.autoscrollok) {
                        FullscreenActivity.clickedOnAutoScrollStart = true;
                        PopUpAutoscrollFragment.this.doSave();
                        mListener.startAutoScroll();
                        PopUpAutoscrollFragment.this.dismiss();
                    }
                }
            });
        }

        mHandler.post(runnable);

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());

        return V;
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        if (mListener!=null) {
            mListener.pageButtonAlpha("");
        }
        mStopHandler = true;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        mStopHandler = true;
        this.dismiss();
    }

    public void doSave() {
        try {
            FullscreenActivity.mPreDelay = popupautoscroll_delay.getProgress()+"";
            FullscreenActivity.mDuration = popupautoscroll_duration.getText().toString();
            if (!popupautoscroll_duration.getText().toString().equals("")) {
                FullscreenActivity.autoScrollDuration = Integer.parseInt(popupautoscroll_duration.getText().toString());
            } else {
                FullscreenActivity.autoScrollDuration = -1;
            }
            PopUpEditSongFragment.prepareSongXML();
            PopUpEditSongFragment.justSaveSongXML(getActivity());
            FullscreenActivity.myToastMessage = getActivity().getResources().getString(R.string.savesong) + " - " +
                    getActivity().getResources().getString(R.string.ok);
            ShowToast.showToast(getActivity());
        } catch (Exception e) {
            e.printStackTrace();
            FullscreenActivity.myToastMessage = getActivity().getResources().getString(R.string.savesong) + " - " +
                    getActivity().getResources().getString(R.string.error);
            ShowToast.showToast(getActivity());
        }
        dismiss();
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

    public void checkAutoScrollStatus() {
        if ((popupautoscroll_duration.getText().toString().equals("") || popupautoscroll_duration.getText().toString().isEmpty()) &&
                !FullscreenActivity.autoscroll_default_or_prompt.equals("default")) {
            String text = getResources().getString(R.string.edit_song_duration) + " - " + getResources().getString(R.string.notset);
            popupautoscroll_startstopbutton.setText(text);
            popupautoscroll_startstopbutton.setEnabled(false);
        } else if (FullscreenActivity.isautoscrolling){
            popupautoscroll_startstopbutton.setText(getResources().getString(R.string.stop));
            popupautoscroll_startstopbutton.setEnabled(true);
            popupautoscroll_startstopbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.stopAutoScroll();
                    PopUpAutoscrollFragment.this.dismiss();
                }
            });
        } else {
            popupautoscroll_startstopbutton.setText(getResources().getString(R.string.start));
            popupautoscroll_startstopbutton.setEnabled(true);
            popupautoscroll_startstopbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.startAutoScroll();
                    PopUpAutoscrollFragment.this.dismiss();
                }
            });
        }
    }
}
