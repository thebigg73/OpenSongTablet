package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

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

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            int myor = getActivity().getResources().getConfiguration().orientation;
            Log.d("d","orientation="+myor);
            PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog());
        }
    }

    Spinner popupPad_key;
    Spinner popupPad_file;
    SwitchCompat popupPad_loopaudio;
    SeekBar popupPad_volume;
    TextView popupPad_volume_text;
    SeekBar popupPad_pan;
    TextView popupPad_pan_text;
    Button savesong;
    Button start_stop_padplay;
    String text;
    boolean validpad;

    boolean mStopHandler = false;
    Handler mHandler = new Handler();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.dismiss();
        }

        getDialog().setTitle(getResources().getString(R.string.pad));
        getDialog().setCanceledOnTouchOutside(true);
        mListener.pageButtonAlpha("pad");

        View V = inflater.inflate(R.layout.popup_page_pad, container, false);

        if (getActivity() != null && getDialog() != null) {
            int myor = getActivity().getResources().getConfiguration().orientation;
            Log.d("d","orientation="+myor);
            PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog());
        }

        // Initialise the views
        popupPad_key = (Spinner) V.findViewById(R.id.popupPad_key);
        popupPad_file = (Spinner) V.findViewById(R.id.popupPad_file);
        popupPad_loopaudio = (SwitchCompat) V.findViewById(R.id.popupPad_loopaudio);
        popupPad_volume = (SeekBar) V.findViewById(R.id.popupPad_volume);
        popupPad_volume_text = (TextView) V.findViewById(R.id.popupPad_volume_text);
        popupPad_pan = (SeekBar) V.findViewById(R.id.popupPad_pan);
        popupPad_pan_text = (TextView) V.findViewById(R.id.popupPad_pan_text);
        savesong = (Button) V.findViewById(R.id.savesong);
        start_stop_padplay = (Button) V.findViewById(R.id.start_stop_padplay);

        //checkPadStatus();

        ArrayAdapter<CharSequence> adapter_key = ArrayAdapter.createFromResource(getActivity(),
                R.array.key_choice,
                R.layout.my_spinner);
        adapter_key.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        popupPad_key.setAdapter(adapter_key);
        ArrayList<String> padfiles = new ArrayList<>();
        padfiles.add(getResources().getString(R.string.pad_auto));
        padfiles.add(getResources().getString(R.string.link_audio));
        padfiles.add(getResources().getString(R.string.off));
        ArrayAdapter<String> adapter_file = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, padfiles);
        adapter_file.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        popupPad_file.setAdapter(adapter_file);

        // Set pad values
        AsyncTask<Object,Void,String> set_pad = new SetPad();
        set_pad.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        ProcessSong.processKey();
        popupPad_key.setSelection(FullscreenActivity.keyindex);

        // Set the listeners
        popupPad_key.setOnItemSelectedListener(new popupPad_keyListener());
        popupPad_file.setOnItemSelectedListener(new popupPad_fileListener());
        popupPad_volume.setOnSeekBarChangeListener(new popupPad_volumeListener());
        popupPad_pan.setOnSeekBarChangeListener(new popupPad_volumeListener());
        popupPad_loopaudio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FullscreenActivity.mLoopAudio = isChecked + "";
                Preferences.savePreferences();
            }
        });
        savesong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopUpEditSongFragment.prepareSongXML();
                try {
                    PopUpEditSongFragment.justSaveSongXML();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Preferences.savePreferences();
                mListener.loadSong();
                dismiss();
            }
        });

        mHandler.post(runnable);

        return V;
    }

    private class popupPad_keyListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            FullscreenActivity.mKey = popupPad_key.getItemAtPosition(popupPad_key.getSelectedItemPosition()).toString();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    }

    private class popupPad_fileListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            if (position == 1) {
                if (FullscreenActivity.mLinkAudio != null &&
                        FullscreenActivity.mLinkAudio.isEmpty() || FullscreenActivity.mLinkAudio.equals("")) {
                    FullscreenActivity.mPadFile = getResources().getString(R.string.pad_auto);
                    popupPad_file.setSelection(0);
                    FullscreenActivity.myToastMessage = getResources().getString(R.string.notset);
                    ShowToast.showToast(getActivity());
                }
            }
            FullscreenActivity.mPadFile = popupPad_file.getItemAtPosition(popupPad_file.getSelectedItemPosition()).toString();
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
            if (FullscreenActivity.mPlayer1 != null) {
                float leftVolume = temp_padvol;
                float rightVolume = temp_padvol;
                if (temp_padpan.equals("left")) {
                    leftVolume = temp_padvol;
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
                    leftVolume = temp_padvol;
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
            FullscreenActivity.padvol = temp_padvol / 100;
            if (popupPad_pan.getProgress() == 0) {
                FullscreenActivity.padpan = "left";
            } else if (popupPad_pan.getProgress() == 2) {
                FullscreenActivity.padpan = "right";
            } else {
                FullscreenActivity.padpan = "both";
            }

            // Save preferences
            Preferences.savePreferences();
        }
    }

    private class SetPad extends AsyncTask<Object,Void,String> {
        @Override
        protected String doInBackground(Object... objects) {
            return null;
        }

        protected void onPostExecute(String s) {
            // Set the pad / backing track
            if (FullscreenActivity.mPadFile.equals(getResources().getString(R.string.off))) {
                popupPad_file.setSelection(2);
            } else if (FullscreenActivity.mPadFile.equals(getResources().getString(R.string.link_audio)) &&
                    !FullscreenActivity.mLinkAudio.isEmpty() && !FullscreenActivity.mLinkAudio.equals("")) {
                popupPad_file.setSelection(1);
            } else {
                popupPad_file.setSelection(0);
            }

            // Set the loop on or off
            if (FullscreenActivity.mLoopAudio.equals("true")) {
                popupPad_loopaudio.setChecked(true);
            } else {
                FullscreenActivity.mLoopAudio = "false";
                popupPad_loopaudio.setChecked(false);
            }

            // Set the pad volume and pan
            int temp_padvol = (int) (100 * FullscreenActivity.padvol);
            popupPad_volume.setProgress(temp_padvol);
            String text = temp_padvol + " %";
            popupPad_volume_text.setText(text);
            switch (FullscreenActivity.padpan) {
                case "left":
                    popupPad_pan_text.setText("L");
                    popupPad_pan.setProgress(0);
                    break;
                case "right":
                    popupPad_pan_text.setText("R");
                    popupPad_pan.setProgress(2);
                    break;
                default:
                    popupPad_pan_text.setText("C");
                    popupPad_pan.setProgress(1);
                    break;
            }

        }
    }

    private void startenabled() {
        validpad = false;
        File file = new File(FullscreenActivity.mLinkAudio);

        if (popupPad_file.getSelectedItemPosition() == 0 && popupPad_key.getSelectedItemPosition() > 0) {
            validpad = true;
        } else if (popupPad_file.getSelectedItemPosition() == 0 && popupPad_key.getSelectedItemPosition() < 1) {
            text = getResources().getString(R.string.pad_choose_key);
            validpad = false;

        } else if (popupPad_file.getSelectedItemPosition() == 1 && file.exists()) {
            validpad = true;
        } else if (popupPad_file.getSelectedItemPosition() == 1 && !file.exists()) {
            validpad = false;
            text = getResources().getString(R.string.link_audio) + " - " + getResources().getString(R.string.notset);

        } else if (popupPad_file.getSelectedItemPosition() == 2) {
            validpad = false;
            text = getResources().getString(R.string.notset);
        }
    }

    public void checkPadStatus() {
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
            validpad = true;
            // start_stop_padplay.setText(getResources().getString(R.string.stop));
            start_stop_padplay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FullscreenActivity.whichPad = 1;
                    FullscreenActivity.pad1Fading = true;
                    mListener.fadeoutPad();
                    dismiss();
                }
            });
        } else if (pad2playing && !pad1playing) {
            text = getResources().getString(R.string.stop);
            validpad = true;
            //start_stop_padplay.setText(getResources().getString(R.string.stop));
            start_stop_padplay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FullscreenActivity.whichPad = 2;
                    FullscreenActivity.pad2Fading = true;
                    mListener.fadeoutPad();
                    dismiss();
                }
            });

        } else if (!pad1playing) {
            text = getResources().getString(R.string.start);
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
            validpad = true;
            //start_stop_padplay.setText(getResources().getString(R.string.stop));
            start_stop_padplay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FullscreenActivity.whichPad = 0;  // both
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
        this.dismiss();
    }

}
