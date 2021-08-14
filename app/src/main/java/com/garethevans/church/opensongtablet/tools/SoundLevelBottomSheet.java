package com.garethevans.church.opensongtablet.tools;

import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.BottomSheetSoundLevelMeterBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;

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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = requireActivity().getWindow();
        if (w != null) {
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetSoundLevelMeterBinding.inflate(inflater, container, false);

        myView.dialogHeader.setText(getString(R.string.volume));
        myView.dialogHeader.setClose(this);

        // Set the values
        setValues();

        // Set the listeners
        setListeners();

        // Initialise audio (and permisisons)
        checkPermissions();

        return myView.getRoot();
    }

    private void setValues() {
        changeRange(mainActivityInterface.getPreferences().getMyPreferenceInt(requireContext(), "soundMeterRange", 400));
    }

    private void setListeners() {
        myView.getRoot().setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED) {
                activityResultLauncher.launch(Manifest.permission.RECORD_AUDIO);
            } else {
                myView.getRoot().setOnClickListener(null);
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
                mainActivityInterface.getPreferences().setMyPreferenceInt(requireContext(),
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


    private void checkPermissions() {
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                if (audio != null) {
                    audio = null;
                }
                int sampleRate = 44100;
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {

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

            } else if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                // Permission hasn't been allowed and we are due to explain why
                try {
                    Snackbar.make(myView.dialogHeader, R.string.storage_rationale,
                            LENGTH_INDEFINITE).setAction(android.R.string.ok, view -> activityResultLauncher.launch(Manifest.permission.RECORD_AUDIO)).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        activityResultLauncher.launch(Manifest.permission.RECORD_AUDIO);
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
            String text = vol + "";
            myView.dBTextView.setText(text);
            String text2 = (int)avvol+"";
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
