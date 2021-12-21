package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class PopUpPadFragment extends DialogFragment {

    static PopUpPadFragment newInstance() {
        PopUpPadFragment frag;
        frag = new PopUpPadFragment();
        return frag;
    }

    public interface MyInterface {
        // IV - gesture used to control activity
        void gesture6();
        void pageButtonAlpha(String s);
        void openFragment();
    }

    private MyInterface mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        mListener = (MyInterface) context;
        super.onAttach(context);
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
    private Preferences preferences;
    private StorageAccess storageAccess;

    private AsyncTask<Object,Void,String> set_pad;

    private boolean mStopHandler = false;
    private final Handler mHandler = new Handler();
    private final Runnable runnable = new Runnable() {
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
        if (getDialog()!=null) {
            getDialog().setCanceledOnTouchOutside(true);
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        try {
            mListener.pageButtonAlpha("pad");
        } catch (Exception e) {
            e.printStackTrace();
        }

        preferences = new Preferences();
        storageAccess = new StorageAccess();
        ProcessSong processSong = new ProcessSong();

        View V = inflater.inflate(R.layout.popup_page_pad, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getString(R.string.pad));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe,getContext());
            closeMe.setEnabled(false);
            // IV - doSave now in dismiss
            PopUpPadFragment.this.dismiss();
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        if (getContext() != null && getDialog() != null) {
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

        ArrayAdapter<CharSequence> adapter_key = ArrayAdapter.createFromResource(getContext(),
                R.array.key_choice,
                R.layout.my_spinner);
        adapter_key.setDropDownViewResource(R.layout.my_spinner);
        popupPad_key.setAdapter(adapter_key);
        ArrayList<String> padfiles = new ArrayList<>();
        padfiles.add(getString(R.string.pad_auto));
        padfiles.add(getString(R.string.link_audio));
        padfiles.add(getString(R.string.off));
        ArrayAdapter<String> adapter_file = new ArrayAdapter<>(getContext(), R.layout.my_spinner, padfiles);
        adapter_file.setDropDownViewResource(R.layout.my_spinner);
        popupPad_file.setAdapter(adapter_file);

        // Set pad values
        set_pad = new SetPad();
        try {
            set_pad.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            Log.d("d","Error setting pad values");
        }

        processSong.processKey(getContext(), preferences, storageAccess);
        popupPad_key.setSelection(FullscreenActivity.keyindex);

        // Set the listeners
        popupPad_key.setOnItemSelectedListener(new popupPad_keyListener());
        popupPad_file.setOnItemSelectedListener(new popupPad_fileListener());
        popupPad_volume.setOnSeekBarChangeListener(new popupPad_volumeListener());
        popupPad_pan.setOnSeekBarChangeListener(new popupPad_volumeListener());
        popupPad_loopaudio.setOnCheckedChangeListener((buttonView, isChecked) -> StaticVariables.mLoopAudio = isChecked + "");

        mHandler.post(runnable);

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    // IV - now called from dismiss
    private void doSave() {
        // We update only if we successfully initialised with pad details
        if (!(popupPad_volume_text.getText() == "")) {
            PopUpEditSongFragment.prepareSongXML();

            if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
                NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper = new NonOpenSongSQLiteHelper(getContext());
                NonOpenSongSQLite nonOpenSongSQLite = nonOpenSongSQLiteHelper.getSong(getContext(), storageAccess, preferences, nonOpenSongSQLiteHelper.getSongId());
                nonOpenSongSQLiteHelper.updateSong(getContext(), storageAccess, preferences, nonOpenSongSQLite);
            } else {
                PopUpEditSongFragment.justSaveSongXML(getContext(), preferences);
            }
            // IV - Start/Stop of pad does not need a song load
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
            // IV - Additional test to prevent volume change affecting a fading pad
            if (!StaticVariables.pad1Fading) {
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

            if (!StaticVariables.pad2Fading) {
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
            preferences.setMyPreferenceFloat(getContext(),"padVol",(float)temp_padvol/100.0f);
            if (popupPad_pan.getProgress() == 0) {
                preferences.setMyPreferenceString(getContext(),"padPan","L");
            } else if (popupPad_pan.getProgress() == 2) {
                preferences.setMyPreferenceString(getContext(),"padPan","R");
            } else {
                preferences.setMyPreferenceString(getContext(),"padPan","C");
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
                if (StaticVariables.mPadFile.equals(getString(R.string.off))) {
                    popupPad_file.setSelection(2);
                } else if (StaticVariables.mPadFile.equals(getString(R.string.link_audio)) && !StaticVariables.mLinkAudio.isEmpty()) {
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
                int temp_padvol = (int) (100 * preferences.getMyPreferenceFloat(getContext(),"padVol",1.0f));
                popupPad_volume.setProgress(temp_padvol);
                String text = temp_padvol + " %";
                popupPad_volume_text.setText(text);
                switch (preferences.getMyPreferenceString(getContext(),"padPan","C")) {
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
                // On change to Audio link, try opening the link file popup to get the user to view/set/delete link audio
                if ((mListener != null) && (!(StaticVariables.mPadFile.equals(getString(R.string.link_audio))))) {
                    FullscreenActivity.whattodo = "page_links";
                    mListener.openFragment();
                    //dismiss();
                }

                if (StaticVariables.mLinkAudio != null && StaticVariables.mLinkAudio.isEmpty()) {
                    StaticVariables.mPadFile = getString(R.string.link_audio);
                    //popupPad_file.setSelection(0);
                    StaticVariables.myToastMessage = getString(R.string.link_audio) + " - " + getString(R.string.notset);
                    ShowToast.showToast(getContext());
                } else {
                    StaticVariables.mPadFile = popupPad_file.getItemAtPosition(popupPad_file.getSelectedItemPosition()).toString();
                }
            } else {
                StaticVariables.mPadFile = popupPad_file.getItemAtPosition(popupPad_file.getSelectedItemPosition()).toString();
            }
            PopUpEditSongFragment.prepareSongXML();
            if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
                NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper = new NonOpenSongSQLiteHelper(getContext());
                NonOpenSongSQLite nonOpenSongSQLite = nonOpenSongSQLiteHelper.getSong(getContext(),storageAccess,preferences,nonOpenSongSQLiteHelper.getSongId());
                nonOpenSongSQLiteHelper.updateSong(getContext(),storageAccess,preferences,nonOpenSongSQLite);
            } else {
                PopUpEditSongFragment.justSaveSongXML(getContext(), preferences);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    private void checkPadStatus() {
        // Proceed if we are not in the middle of an Async song load!
        if (!FullscreenActivity.alreadyloading) {
            if (((StaticVariables.pad1Playing && !StaticVariables.pad1Fading) || (StaticVariables.pad2Playing & !StaticVariables.pad2Fading))) {
                start_stop_padplay.setText(getString(R.string.stop));
            } else {
                start_stop_padplay.setText(getString(R.string.start));
            }

            start_stop_padplay.setOnClickListener(view -> {
            // IV - gesture6 has the start and stop logic
                mListener.gesture6();
                PopUpPadFragment.this.dismiss();
            });
        }
    }

    @Override
    public void onDismiss(@NonNull final DialogInterface dialog) {
        if (mListener!=null) {
            mListener.pageButtonAlpha("");
        }
        mStopHandler = true;
        // IV - Always save
        doSave();
        // IV - Moved to dismiss
        if (set_pad!=null) {
            set_pad.cancel(true);
        }
        super.onDismiss(dialog);
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        this.dismiss();
    }

}