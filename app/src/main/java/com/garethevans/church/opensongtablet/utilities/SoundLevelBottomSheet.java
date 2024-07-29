package com.garethevans.church.opensongtablet.utilities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.appdata.InformationBottomSheet;
import com.garethevans.church.opensongtablet.databinding.BottomSheetSoundLevelMeterBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.slider.Slider;

public class SoundLevelBottomSheet extends BottomSheetDialogFragment {

    private AudioRecord audio;
    private int bufferSize;
    private MainActivityInterface mainActivityInterface;
    private BottomSheetSoundLevelMeterBinding myView;

    private int totalvols = 0;
    private int counts = 0;
    private float avvol = 0.0f;
    private final Runnable r = this::sampleSound;
    private Handler audioRecordHandler;
    ActivityResultLauncher<String> activityResultLauncher;
    private String volume_string="", website_sound_level_meter_string="", microphone_string="",
            permissions_refused_string="", settings_string="";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = null;
        if (getActivity()!=null) {
            w = getActivity().getWindow();
        }
        if (w != null) {
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialog1 -> {
            FrameLayout bottomSheet = ((BottomSheetDialog) dialog1).findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetBehavior.from(bottomSheet).setDraggable(false);
            }
        });
        return dialog;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetSoundLevelMeterBinding.inflate(inflater, container, false);

        prepareString();

        myView.dialogHeader.setText(volume_string);
        myView.dialogHeader.setClose(this);
        myView.dialogHeader.setWebHelp(mainActivityInterface, website_sound_level_meter_string);

        // Set the values
        setValues();

        // Set the listeners
        setListeners();

        // Initialise audio (and permisisons)
        checkPermissions();

        return myView.getRoot();
    }

    private void prepareString() {
        if (getContext()!=null) {
            volume_string = getString(R.string.volume);
            website_sound_level_meter_string = getString(R.string.website_sound_level_meter);
            microphone_string = getString(R.string.microphone);
            permissions_refused_string = getString(R.string.permissions_refused);
            settings_string = getString(R.string.settings);
        }
    }
    private void setValues() {
        changeRange(mainActivityInterface.getPreferences().getMyPreferenceInt("soundMeterRange", 400));
    }

    private void setListeners() {
        myView.getRoot().setOnClickListener(v -> {
            if (!mainActivityInterface.getAppPermissions().hasAudioPermissions()) {
                activityResultLauncher.launch(mainActivityInterface.getAppPermissions().getAudioPermissions());
            }
        });
        myView.resetaverage.setOnClickListener(v -> {
            totalvols = 0;
            counts = 0;
            avvol = 0.0f;
        });
        myView.maxvolrange.addOnChangeListener((slider, value, fromUser) -> changeRange((int) value));
        myView.maxvolrange.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) { }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                mainActivityInterface.getPreferences().setMyPreferenceInt(
                        "soundMeterRange", (int) myView.maxvolrange.getValue());
            }
        });
    }

    private void changeRange(int range) {
        if (range>myView.maxvolrange.getValueTo()) {
            range = (int) myView.maxvolrange.getValueTo();
        }
        myView.maxvolrange.setValue(range);
        myView.maxvolrange.setHint("0 - " + range);
    }

    @SuppressLint("MissingPermission") // Checked in getAppPermissions
    private void checkPermissions() {
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                if (audio != null) {
                    audio = null;
                }
                int sampleRate = 44100;
                if (mainActivityInterface.getAppPermissions().hasAudioPermissions()) {

                    try {
                        bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO,
                                AudioFormat.ENCODING_PCM_16BIT);

                        audio = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
                                AudioFormat.CHANNEL_IN_MONO,
                                AudioFormat.ENCODING_PCM_16BIT, bufferSize);
                    } catch (Exception e) {
                        android.util.Log.e("TrackingFlow", "Exception", e);
                    }

                    try {
                        audio.startRecording();
                        audioRecordHandler = new Handler();
                        audioRecordHandler.postDelayed(r, 50);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            } else  {
            // notify user
            InformationBottomSheet informationBottomSheet = new InformationBottomSheet(microphone_string,
                    permissions_refused_string, settings_string, "appPrefs");
            informationBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "InformationBottomSheet");
            }
        });
        activityResultLauncher.launch(mainActivityInterface.getAppPermissions().getAudioPermissions());
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (audioRecordHandler!=null) {
            try {
                audioRecordHandler.removeCallbacks(r);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (audio!=null) {
            try {
                audio.stop();
                audio.release();
                audio = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void sampleSound() {
        short[] buffer = new short[bufferSize];
        int bufferReadResult;
        if (audio != null) {
            // Sense the voice...
            bufferReadResult = audio.read(buffer, 0, bufferSize);
            double sumLevel = 0;
            for (int i = 0; i < bufferReadResult; i++) {
                sumLevel += buffer[i];
            }
            double lastLevel = Math.abs((sumLevel / bufferReadResult));
            int vol = (int) Math.round(lastLevel);
            totalvols += vol;
            counts ++;
            avvol = (float) totalvols/(float) counts;
            avvol = Math.round(avvol);
            String text = String.valueOf(vol);
            myView.dBTextView.setText(text);
            String text2 = String.valueOf((int)avvol);
            myView.averagevol.setText(text2);

            // Turn the appropriate level lights on or off
            adjustVolumeLights(vol);

            audioRecordHandler.removeCallbacks(r);
            audioRecordHandler.post(r);
        }
    }

    private void adjustVolumeLights(float vol) {
        float maxVol = myView.maxvolrange.getValue();
        // Show the lights if they are above the float percentages
        visibilityChange(myView.level10,vol>(0.9f*maxVol));
        visibilityChange(myView.level9,vol>(0.8f*maxVol));
        visibilityChange(myView.level8,vol>(0.7f*maxVol));
        visibilityChange(myView.level7,vol>(0.6f*maxVol));
        visibilityChange(myView.level6,vol>(0.5f*maxVol));
        visibilityChange(myView.level5,vol>(0.4f*maxVol));
        visibilityChange(myView.level4,vol>(0.3f*maxVol));
        visibilityChange(myView.level3,vol>(0.2f*maxVol));
        visibilityChange(myView.level2,vol>(0.1f*maxVol));
        visibilityChange(myView.level1,vol>0.0f);

    }
    private void visibilityChange(View view, boolean visible) {
        if (visible) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.INVISIBLE);
        }
    }

}
