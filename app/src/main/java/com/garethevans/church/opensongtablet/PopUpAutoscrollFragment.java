package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Objects;

public class PopUpAutoscrollFragment extends DialogFragment {

    static PopUpAutoscrollFragment newInstance() {
        PopUpAutoscrollFragment frag;
        frag = new PopUpAutoscrollFragment();
        return frag;
    }

    public interface MyInterface {
        void pageButtonAlpha(String s);
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

    private Button popupautoscroll_startstopbutton;
    private SeekBar popupautoscroll_delay;
    private TextView popupautoscroll_delay_text;
    private EditText popupautoscroll_duration;
    private FloatingActionButton uselinkaudiolength_ImageButton;
    private Preferences preferences;
    private StorageAccess storageAccess;
    private ProcessSong processSong;

    private boolean mStopHandler = false;
    private final Handler mHandler = new Handler();
    private final Runnable runnable = new Runnable() {
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.dismiss();
        }
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        mListener.pageButtonAlpha("autoscroll");

        View V = inflater.inflate(R.layout.popup_page_autoscroll, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.autoscroll));
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
        saveMe.hide();

        preferences = new Preferences();
        processSong = new ProcessSong();
        storageAccess = new StorageAccess();

        // Initialise the views
        popupautoscroll_startstopbutton = V.findViewById(R.id.popupautoscroll_startstopbutton);
        Button popupautoscroll_learnbutton = V.findViewById(R.id.popupautoscroll_learnbutton);
        // Don't allow the learn feature for multipage pdfs - too complex for noe
        if (FullscreenActivity.isPDF && StaticVariables.pdfPageCount>1) {
            popupautoscroll_learnbutton.setVisibility(View.GONE);
        }
        popupautoscroll_delay = V.findViewById(R.id.popupautoscroll_delay);
        popupautoscroll_delay_text = V.findViewById(R.id.popupautoscroll_delay_text);
        popupautoscroll_duration = V.findViewById(R.id.popupautoscroll_duration);
        uselinkaudiolength_ImageButton = V.findViewById(R.id.uselinkaudiolength_ImageButton);
        popupautoscroll_delay.setMax(preferences.getMyPreferenceInt(getActivity(),"autoscrollDefaultMaxPreDelay",30));
        // Set up current values
        AutoScrollFunctions.getAutoScrollTimes(getActivity(),preferences);
        String text;
        if (StaticVariables.autoScrollDelay < 0) {
            popupautoscroll_delay.setProgress(0);
            text = "";
        } else {
            popupautoscroll_delay.setProgress(StaticVariables.autoScrollDelay + 1);
            text = StaticVariables.autoScrollDelay + " s";
        }
        popupautoscroll_delay_text.setText(text);

        String text2 = StaticVariables.autoScrollDuration + "";
        if (StaticVariables.autoScrollDuration>-1) {
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
                StaticVariables.mDuration = textView.getText().toString();
                StaticVariables.autoscrollok = processSong.isAutoScrollValid(getActivity(),preferences);
                return false;
            }
        });
        popupautoscroll_delay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                StaticVariables.mPreDelay = ""+i;
                String s = i + " s";
                popupautoscroll_delay_text.setText(s);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                StaticVariables.autoscrollok = processSong.isAutoScrollValid(getActivity(),preferences);
            }
        });
        uselinkaudiolength_ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(uselinkaudiolength_ImageButton, PopUpAutoscrollFragment.this.getActivity());
                PopUpAutoscrollFragment.this.grabLinkAudioTime();
                StaticVariables.autoscrollok = processSong.isAutoScrollValid(getActivity(),preferences);
            }
        });
        if (StaticVariables.isautoscrolling) {
            popupautoscroll_startstopbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    StaticVariables.clickedOnAutoScrollStart = false;
                    mListener.stopAutoScroll();
                    PopUpAutoscrollFragment.this.dismiss();
                }
            });
            popupautoscroll_startstopbutton.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.stop));

        } else {
            popupautoscroll_startstopbutton.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.start));
            popupautoscroll_startstopbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (StaticVariables.autoscrollok) {
                        StaticVariables.clickedOnAutoScrollStart = true;
                        PopUpAutoscrollFragment.this.doSave();
                        mListener.startAutoScroll();
                        PopUpAutoscrollFragment.this.dismiss();
                    }
                }
            });
        }

        mHandler.post(runnable);

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

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

    private void doSave() {
        try {
            StaticVariables.mPreDelay = popupautoscroll_delay.getProgress()+"";
            StaticVariables.mDuration = popupautoscroll_duration.getText().toString();
            if (!popupautoscroll_duration.getText().toString().equals("")) {
                StaticVariables.autoScrollDuration = Integer.parseInt(popupautoscroll_duration.getText().toString());
            } else {
                StaticVariables.autoScrollDuration = -1;
            }
            PopUpEditSongFragment.prepareSongXML();
            if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
                NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper = new NonOpenSongSQLiteHelper(getActivity());
                NonOpenSongSQLite nonOpenSongSQLite = nonOpenSongSQLiteHelper.getSong(getActivity(),storageAccess,preferences,nonOpenSongSQLiteHelper.getSongId());
                nonOpenSongSQLiteHelper.updateSong(getActivity(),storageAccess,preferences,nonOpenSongSQLite);
            } else {
                PopUpEditSongFragment.justSaveSongXML(getActivity(), preferences);
            }
            StaticVariables.myToastMessage = Objects.requireNonNull(getActivity()).getResources().getString(R.string.save) + " - " +
                    getActivity().getResources().getString(R.string.ok);
            ShowToast.showToast(getActivity());
        } catch (Exception e) {
            e.printStackTrace();
            StaticVariables.myToastMessage = Objects.requireNonNull(getActivity()).getResources().getString(R.string.save) + " - " +
                    getActivity().getResources().getString(R.string.error);
            ShowToast.showToast(getActivity());
        }
        dismiss();
    }

    private void grabLinkAudioTime() {
        AutoScrollFunctions.getAudioLength(getActivity(), preferences);
        if (StaticVariables.audiolength>-1) {
            // If this is a valid audio length, set the mDuration value
            StaticVariables.mDuration = "" + StaticVariables.audiolength;
            popupautoscroll_duration.setText(StaticVariables.mDuration);
        } else {
            StaticVariables.myToastMessage = Objects.requireNonNull(getActivity()).getResources().getString(R.string.link_audio) + " - " +
                    getActivity().getResources().getString(R.string.notset);
            ShowToast.showToast(getActivity());
        }
    }

    private void checkAutoScrollStatus() {
        if ((popupautoscroll_duration.getText().toString().equals("") || popupautoscroll_duration.getText().toString().isEmpty()) &&
                !preferences.getMyPreferenceBoolean(getActivity(),"autoscrollUseDefaultTime",false)) {
            String text = getResources().getString(R.string.edit_song_duration) + " - " + getResources().getString(R.string.notset);
            popupautoscroll_startstopbutton.setText(text);
            popupautoscroll_startstopbutton.setEnabled(false);
        } else if (StaticVariables.isautoscrolling){
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
