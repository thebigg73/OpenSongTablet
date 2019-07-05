package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

public class PopUpPadFragment extends DialogFragment {

    static PopUpPadFragment newInstance() {
        PopUpPadFragment frag;
        frag = new PopUpPadFragment();
        return frag;
    }

    public interface MyInterface {
        void loadSong();
        void fadeoutPad();
        void preparePad();
        void killPad();
        void pageButtonAlpha(String s);
        void openFragment();
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
        mStopHandler = true;
        super.onDetach();
    }

    private Spinner popupPad_key;
    private Spinner popupPad_file;
    private SwitchCompat popupPad_loopaudio;
    private SeekBar popupPad_volume;
    private TextView popupPad_volume_text;
    private SeekBar popupPad_pan;
    private TextView popupPad_pan_text;
    private Button start_stop_padplay;
    String text;
    private boolean validpad;
    Preferences preferences;
    StorageAccess storageAccess;
    ProcessSong processSong;

    private AsyncTask<Object,Void,String> set_pad;

    private boolean mStopHandler = false;
    private Handler mHandler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                checkPadStatus();
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
        mListener.pageButtonAlpha("pad");

        preferences = new Preferences();
        storageAccess = new StorageAccess();
        processSong = new ProcessSong();

        View V = inflater.inflate(R.layout.popup_page_pad, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.pad));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                doSave();
            }
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog(), preferences);
        }

        // Initialise the views
        popupPad_key = V.findViewById(R.id.popupPad_key);
        popupPad_file = V.findViewById(R.id.popupPad_file);
        popupPad_loopaudio = V.findViewById(R.id.popupPad_loopaudio);
        popupPad_volume = V.findViewById(R.id.popupPad_volume);
        popupPad_volume_text = V.findViewById(R.id.popupPad_volume_text);
        popupPad_pan = V.findViewById(R.id.popupPad_pan);
        popupPad_pan_text = V.findViewById(R.id.popupPad_pan_text);
        start_stop_padplay = V.findViewById(R.id.start_stop_padplay);

        ArrayAdapter<CharSequence> adapter_key = ArrayAdapter.createFromResource(getActivity(),
                R.array.key_choice,
                R.layout.my_spinner);
        adapter_key.setDropDownViewResource(R.layout.my_spinner);
        popupPad_key.setAdapter(adapter_key);
        ArrayList<String> padfiles = new ArrayList<>();
        padfiles.add(getResources().getString(R.string.pad_auto));
        padfiles.add(getResources().getString(R.string.link_audio));
        padfiles.add(getResources().getString(R.string.off));
        ArrayAdapter<String> adapter_file = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, padfiles);
        adapter_file.setDropDownViewResource(R.layout.my_spinner);
        popupPad_file.setAdapter(adapter_file);

        // Set pad values
        set_pad = new SetPad();
        try {
            set_pad.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            Log.d("d","Error setting pad values");
        }

        processSong.processKey(getActivity(), preferences, storageAccess);
        popupPad_key.setSelection(FullscreenActivity.keyindex);

        // Set the listeners
        popupPad_key.setOnItemSelectedListener(new popupPad_keyListener());
        popupPad_file.setOnItemSelectedListener(new popupPad_fileListener());
        popupPad_volume.setOnSeekBarChangeListener(new popupPad_volumeListener());
        popupPad_pan.setOnSeekBarChangeListener(new popupPad_volumeListener());
        popupPad_loopaudio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                StaticVariables.mLoopAudio = isChecked + "";
            }
        });

        mHandler.post(runnable);

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    public void doSave() {
        PopUpEditSongFragment.prepareSongXML();
        PopUpEditSongFragment.justSaveSongXML(getActivity(), preferences);
        try {
            mListener.loadSong();
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class popupPad_keyListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            StaticVariables.mKey = popupPad_key.getItemAtPosition(popupPad_key.getSelectedItemPosition()).toString();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    }

    private void startenabled() {
        validpad = false;
        StorageAccess storageAccess = new StorageAccess();
        Uri uri = storageAccess.fixLocalisedUri(getActivity(), preferences, StaticVariables.mLinkAudio);
        boolean isvalid = storageAccess.uriExists(getActivity(), uri);

        if (popupPad_file.getSelectedItemPosition() == 0 && popupPad_key.getSelectedItemPosition() > 0) {
            validpad = true;
        } else if (popupPad_file.getSelectedItemPosition() == 0 && popupPad_key.getSelectedItemPosition() < 1) {
            text = getResources().getString(R.string.pad_choose_key);
            validpad = false;

        } else if (popupPad_file.getSelectedItemPosition() == 1 && isvalid) {
            validpad = true;
        } else if (popupPad_file.getSelectedItemPosition() == 1 && !isvalid) {
            validpad = false;
            text = getResources().getString(R.string.link_audio) + " - " + getResources().getString(R.string.notset);

        } else if (popupPad_file.getSelectedItemPosition() == 2) {
            validpad = false;
            text = getResources().getString(R.string.notset);
        }
    }

    private class popupPad_volumeListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            String text = popupPad_volume.getProgress() + " %";
            popupPad_volume_text.setText(text);
            float temp_padvol = (float) popupPad_volume.getProgress() / 100.0f;
            String temp_padpan = "both";
            if (popupPad_pan.getProgress() == 0) {
                temp_padpan = "left";
                popupPad_pan_text.setText("L");
            } else if (popupPad_pan.getProgress() == 2) {
                temp_padpan = "right";
                popupPad_pan_text.setText("R");
            } else {
                popupPad_pan_text.setText("C");
            }
            if (FullscreenActivity.mPlayer1 != null) {
                float leftVolume = temp_padvol;
                float rightVolume = temp_padvol;
                if (temp_padpan.equals("left")) {
                    //leftVolume = temp_padvol;
                    rightVolume = 0.0f;
                } else if (temp_padpan.equals("right")) {
                    leftVolume = 0.0f;
                    rightVolume = temp_padvol;
                }
                try {
                    FullscreenActivity.mPlayer1.setVolume(leftVolume, rightVolume);
                } catch (Exception e) {
                    // This will catch any exception, because they are all descended from Exception
                }
            }

            if (FullscreenActivity.mPlayer2 != null) {

                float leftVolume = temp_padvol;
                float rightVolume = temp_padvol;
                if (temp_padpan.equals("left")) {
                    //leftVolume = temp_padvol;
                    rightVolume = 0.0f;
                } else if (temp_padpan.equals("right")) {
                    leftVolume = 0.0f;
                    rightVolume = temp_padvol;
                }
                try {
                    FullscreenActivity.mPlayer2.setVolume(leftVolume, rightVolume);
                } catch (Exception e) {
                    // This will catch any exception, because they are all descended from Exception
                }
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            int temp_padvol = popupPad_volume.getProgress();
            preferences.setMyPreferenceFloat(getActivity(),"padVol",(float)temp_padvol/100.0f);
            if (popupPad_pan.getProgress() == 0) {
                preferences.setMyPreferenceString(getActivity(),"padPan","L");
            } else if (popupPad_pan.getProgress() == 2) {
                preferences.setMyPreferenceString(getActivity(),"padPan","R");
            } else {
                preferences.setMyPreferenceString(getActivity(),"padPan","C");
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class SetPad extends AsyncTask<Object,Void,String> {
        @Override
        protected String doInBackground(Object... objects) {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            return null;
        }

        protected void onPostExecute(String s) {
            // Set the pad / backing track
            try {
                if (StaticVariables.mPadFile.equals(getResources().getString(R.string.off))) {
                    popupPad_file.setSelection(2);
                } else if (StaticVariables.mPadFile.equals(getResources().getString(R.string.link_audio)) && !StaticVariables.mLinkAudio.isEmpty()) {
                    popupPad_file.setSelection(1);
                } else {
                    popupPad_file.setSelection(0);
                }

                // Set the loop on or off
                if (StaticVariables.mLoopAudio.equals("true")) {
                    popupPad_loopaudio.setChecked(true);
                } else {
                    StaticVariables.mLoopAudio = "false";
                    popupPad_loopaudio.setChecked(false);
                }

                // Set the pad volume and pan
                int temp_padvol = (int) (100 * preferences.getMyPreferenceFloat(getActivity(),"padVol",1.0f));
                popupPad_volume.setProgress(temp_padvol);
                String text = temp_padvol + " %";
                popupPad_volume_text.setText(text);
                switch (preferences.getMyPreferenceString(getActivity(),"padPan","C")) {
                    case "L":
                        popupPad_pan_text.setText("L");
                        popupPad_pan.setProgress(0);
                        break;
                    case "R":
                        popupPad_pan_text.setText("R");
                        popupPad_pan.setProgress(2);
                        break;
                    default:
                        popupPad_pan_text.setText("C");
                        popupPad_pan.setProgress(1);
                        break;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class popupPad_fileListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            if (position == 1) {
                if (StaticVariables.mLinkAudio != null && StaticVariables.mLinkAudio.isEmpty()) {
                    StaticVariables.mPadFile = getResources().getString(R.string.link_audio);
                    //popupPad_file.setSelection(0);
                    StaticVariables.myToastMessage = getResources().getString(R.string.notset);
                    ShowToast.showToast(getActivity());
                    // Try opening the link file popup to get the user to set one
                    if (mListener != null) {
                        FullscreenActivity.whattodo = "page_links";
                        mListener.openFragment();
                        //dismiss();
                    }
                } else {
                    StaticVariables.mPadFile = popupPad_file.getItemAtPosition(popupPad_file.getSelectedItemPosition()).toString();
                }
            } else {
                StaticVariables.mPadFile = popupPad_file.getItemAtPosition(popupPad_file.getSelectedItemPosition()).toString();
            }
            PopUpEditSongFragment.prepareSongXML();
            PopUpEditSongFragment.justSaveSongXML(getActivity(), preferences);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    private void checkPadStatus() {
        boolean pad1playing = false;
        boolean pad2playing = false;
        try {
            pad1playing = FullscreenActivity.mPlayer1 != null && FullscreenActivity.mPlayer1.isPlaying();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            pad2playing = FullscreenActivity.mPlayer2 != null && FullscreenActivity.mPlayer2.isPlaying();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (pad1playing && !pad2playing) {
            text = getResources().getString(R.string.stop);
            StaticVariables.clickedOnPadStart = true;
            validpad = true;
            start_stop_padplay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FullscreenActivity.whichPad = 1;
                    StaticVariables.pad1Fading = true;
                    mListener.fadeoutPad();
                    StaticVariables.clickedOnPadStart = false;
                    dismiss();
                }
            });
        } else if (pad2playing && !pad1playing) {
            text = getResources().getString(R.string.stop);
            StaticVariables.clickedOnPadStart = true;
            validpad = true;
            //start_stop_padplay.setText(getResources().getString(R.string.stop));
            start_stop_padplay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FullscreenActivity.whichPad = 2;
                    StaticVariables.pad2Fading = true;
                    StaticVariables.clickedOnPadStart = false;
                    mListener.fadeoutPad();
                    dismiss();
                }
            });

        } else if (!pad1playing) {
            text = getResources().getString(R.string.start);
            StaticVariables.clickedOnPadStart = false;
            // start_stop_padplay.setText(getResources().getString(R.string.start));
            // Decide if pad is valid
            startenabled();
            start_stop_padplay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FullscreenActivity.whichPad = 0;
                    mListener.killPad();
                    FullscreenActivity.whichPad = 1;
                    mListener.preparePad();
                    dismiss();
                }
            });

        } else {
            text = getResources().getString(R.string.stop);
            StaticVariables.clickedOnPadStart = true;
            validpad = true;
            //start_stop_padplay.setText(getResources().getString(R.string.stop));
            start_stop_padplay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FullscreenActivity.whichPad = 0;  // both
                    StaticVariables.clickedOnPadStart = false;
                    mListener.killPad();
                    dismiss();
                }
            });
        }

        start_stop_padplay.setText(text);
        start_stop_padplay.setEnabled(validpad);
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        if (mListener!=null) {
            mListener.pageButtonAlpha("");
        }
        mStopHandler = true;
        super.onDismiss(dialog);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        mStopHandler = true;
        if (set_pad!=null) {
            set_pad.cancel(true);
        }
        this.dismiss();
    }

}