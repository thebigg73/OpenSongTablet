/*
package com.garethevans.church.opensongtablet;

import android.app.Activity;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PopUpSoundLevelMeterFragment extends DialogFragment {

    private AudioRecord audio;
    private int bufferSize;

    static PopUpSoundLevelMeterFragment newInstance() {
        PopUpSoundLevelMeterFragment frag;
        frag = new PopUpSoundLevelMeterFragment();
        return frag;
    }

    private TextView dBTextView;
    private Handler mHandlerStart;
    private ImageView level_1;
    private ImageView level_2;
    private ImageView level_3;
    private ImageView level_4;
    private ImageView level_5;
    private ImageView level_6;
    private ImageView level_7;
    private ImageView level_8;
    private ImageView level_9;
    private ImageView level_10;
    private TextView volume_value;
    private TextView averagevol;
    private int totalvols = 0;
    private int counts = 0;
    private float avvol = 0.0f;

    private Preferences preferences;

    private final Runnable r = this::sampleSound;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog()!=null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(true);
        }
        View V = inflater.inflate(R.layout.popup_soundlevelmeter, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getString(R.string.volume));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe,getContext());
            closeMe.setEnabled(false);
            dismiss();
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        preferences = new Preferences();

        dBTextView = V.findViewById(R.id.dBTextView);

        level_1 = V.findViewById(R.id.level_1);
        level_2 = V.findViewById(R.id.level_2);
        level_3 = V.findViewById(R.id.level_3);
        level_4 = V.findViewById(R.id.level_4);
        level_5 = V.findViewById(R.id.level_5);
        level_6 = V.findViewById(R.id.level_6);
        level_7 = V.findViewById(R.id.level_7);
        level_8 = V.findViewById(R.id.level_8);
        level_9 = V.findViewById(R.id.level_9);
        level_10 = V.findViewById(R.id.level_10);

        averagevol = V.findViewById(R.id.averagevol);
        Button resetaverage = V.findViewById(R.id.resetaverage);

        resetaverage.setOnClickListener(v -> {
            totalvols = 0;
            counts = 0;
            avvol = 0.0f;
        });

        volume_value = V.findViewById(R.id.volume_value);

        SeekBar maxvolrange = V.findViewById(R.id.maxvolrange);
        maxvolrange.setMax(7);
        int myprogress;
        String mytext;
        switch (preferences.getMyPreferenceInt(getContext(),"soundMeterRange",400)) {
            case 50:
                myprogress = 0;
                mytext = "0 - 50";
                break;

            case 100:
                myprogress = 1;
                mytext = "0 - 100";
                break;

            case 400:
                myprogress = 3;
                mytext = "0 - 400";
                break;

            case 600:
                myprogress = 4;
                mytext = "0 - 600";
                break;

            case 1000:
                myprogress = 5;
                mytext = "0 - 1000";
                break;

            case 2000:
                myprogress = 6;
                mytext = "0 - 2000";
                break;

            case 3000:
                myprogress = 7;
                mytext = "0 - 3000";
                break;

            case 200:
            default:
                myprogress = 2;
                mytext = "0 - 200";
                break;
        }
        maxvolrange.setProgress(myprogress);
        volume_value.setText(mytext);

        maxvolrange.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //volume_value.setText(progress);
                int volrangechosen;
                String text;
                switch (progress) {
                    case 0:
                        volrangechosen = 50;
                        text = "0 - 50";
                        break;
                    case 1:
                        volrangechosen = 100;
                        text = "0 - 100";
                        break;
                    case 2:
                        volrangechosen = 200;
                        text = "0 - 200";
                        break;
                    case 3:
                        volrangechosen = 400;
                        text = "0 - 400";
                        break;
                    case 4:
                        volrangechosen = 600;
                        text = "0 - 600";
                        break;
                    case 5:
                        volrangechosen = 1000;
                        text = "0 - 1000";
                        break;
                    case 6:
                        volrangechosen = 2000;
                        text = "0 - 2000";
                        break;
                    case 7:
                        volrangechosen = 3000;
                        text = "0 - 3000";
                        break;

                    default:
                        volrangechosen = 100;
                        text = "0-100";
                        break;
                }

                preferences.setMyPreferenceInt(getContext(),"soundMeterRange",volrangechosen);
                volume_value.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        if (audio!=null) {
            audio=null;
        }

        int sampleRate = 44100;
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
            mHandlerStart = new Handler();
            mHandlerStart.postDelayed(r, 50);
        } catch (Exception e) {
            e.printStackTrace();
        }

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);
        return V;
    }

    @Override
    public void onDismiss(@NonNull final DialogInterface dialog) {
        super.onDismiss(dialog);
        // IV - Remove any pending callback
        mHandlerStart.removeCallbacks(r);
        try {
            audio.stop();
            audio.release();
            audio = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        final Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
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
            dBTextView.setText(text);
            String text2 = (int)avvol+"";
            averagevol.setText(text2);

            // Turn the appropriate level lights on or off
            // Assume the highest value is 170

            float maxvolrange = (float) preferences.getMyPreferenceInt(getContext(),"soundMeterRange",400);
            if (vol>(0.9*maxvolrange)) {
                // All 10 levels on
                level_1.setVisibility(View.VISIBLE);
                level_2.setVisibility(View.VISIBLE);
                level_3.setVisibility(View.VISIBLE);
                level_4.setVisibility(View.VISIBLE);
                level_5.setVisibility(View.VISIBLE);
                level_6.setVisibility(View.VISIBLE);
                level_7.setVisibility(View.VISIBLE);
                level_8.setVisibility(View.VISIBLE);
                level_9.setVisibility(View.VISIBLE);
                level_10.setVisibility(View.VISIBLE);
            } else if (vol>(0.8*maxvolrange)) {
                // 9 levels on
                level_1.setVisibility(View.VISIBLE);
                level_2.setVisibility(View.VISIBLE);
                level_3.setVisibility(View.VISIBLE);
                level_4.setVisibility(View.VISIBLE);
                level_5.setVisibility(View.VISIBLE);
                level_6.setVisibility(View.VISIBLE);
                level_7.setVisibility(View.VISIBLE);
                level_8.setVisibility(View.VISIBLE);
                level_9.setVisibility(View.VISIBLE);
                level_10.setVisibility(View.INVISIBLE);
            } else if (vol>(0.7*maxvolrange)) {
                // 8 levels on
                level_1.setVisibility(View.VISIBLE);
                level_2.setVisibility(View.VISIBLE);
                level_3.setVisibility(View.VISIBLE);
                level_4.setVisibility(View.VISIBLE);
                level_5.setVisibility(View.VISIBLE);
                level_6.setVisibility(View.VISIBLE);
                level_7.setVisibility(View.VISIBLE);
                level_8.setVisibility(View.VISIBLE);
                level_9.setVisibility(View.INVISIBLE);
                level_10.setVisibility(View.INVISIBLE);
            } else if (vol>(0.6*maxvolrange)) {
                // 7 levels on
                level_1.setVisibility(View.VISIBLE);
                level_2.setVisibility(View.VISIBLE);
                level_3.setVisibility(View.VISIBLE);
                level_4.setVisibility(View.VISIBLE);
                level_5.setVisibility(View.VISIBLE);
                level_6.setVisibility(View.VISIBLE);
                level_7.setVisibility(View.VISIBLE);
                level_8.setVisibility(View.INVISIBLE);
                level_9.setVisibility(View.INVISIBLE);
                level_10.setVisibility(View.INVISIBLE);
            } else if (vol>(0.5*maxvolrange)) {
                // 6 levels on
                level_1.setVisibility(View.VISIBLE);
                level_2.setVisibility(View.VISIBLE);
                level_3.setVisibility(View.VISIBLE);
                level_4.setVisibility(View.VISIBLE);
                level_5.setVisibility(View.VISIBLE);
                level_6.setVisibility(View.VISIBLE);
                level_7.setVisibility(View.INVISIBLE);
                level_8.setVisibility(View.INVISIBLE);
                level_9.setVisibility(View.INVISIBLE);
                level_10.setVisibility(View.INVISIBLE);
            } else if (vol>(0.4*maxvolrange)) {
                // 5 levels on
                level_1.setVisibility(View.VISIBLE);
                level_2.setVisibility(View.VISIBLE);
                level_3.setVisibility(View.VISIBLE);
                level_4.setVisibility(View.VISIBLE);
                level_5.setVisibility(View.VISIBLE);
                level_6.setVisibility(View.INVISIBLE);
                level_7.setVisibility(View.INVISIBLE);
                level_8.setVisibility(View.INVISIBLE);
                level_9.setVisibility(View.INVISIBLE);
                level_10.setVisibility(View.INVISIBLE);
            } else if (vol>(0.3*maxvolrange)) {
                // 4 levels on
                level_1.setVisibility(View.VISIBLE);
                level_2.setVisibility(View.VISIBLE);
                level_3.setVisibility(View.VISIBLE);
                level_4.setVisibility(View.VISIBLE);
                level_5.setVisibility(View.INVISIBLE);
                level_6.setVisibility(View.INVISIBLE);
                level_7.setVisibility(View.INVISIBLE);
                level_8.setVisibility(View.INVISIBLE);
                level_9.setVisibility(View.INVISIBLE);
                level_10.setVisibility(View.INVISIBLE);
            } else if (vol>(0.2*maxvolrange)) {
                // 3 levels on
                level_1.setVisibility(View.VISIBLE);
                level_2.setVisibility(View.VISIBLE);
                level_3.setVisibility(View.VISIBLE);
                level_4.setVisibility(View.INVISIBLE);
                level_5.setVisibility(View.INVISIBLE);
                level_6.setVisibility(View.INVISIBLE);
                level_7.setVisibility(View.INVISIBLE);
                level_8.setVisibility(View.INVISIBLE);
                level_9.setVisibility(View.INVISIBLE);
                level_10.setVisibility(View.INVISIBLE);
            } else if (vol>(0.1*maxvolrange)) {
                // 2 levels on
                level_1.setVisibility(View.VISIBLE);
                level_2.setVisibility(View.VISIBLE);
                level_3.setVisibility(View.INVISIBLE);
                level_4.setVisibility(View.INVISIBLE);
                level_5.setVisibility(View.INVISIBLE);
                level_6.setVisibility(View.INVISIBLE);
                level_7.setVisibility(View.INVISIBLE);
                level_8.setVisibility(View.INVISIBLE);
                level_9.setVisibility(View.INVISIBLE);
                level_10.setVisibility(View.INVISIBLE);
            } else {
                // 1 levels on
                level_1.setVisibility(View.VISIBLE);
                level_2.setVisibility(View.INVISIBLE);
                level_3.setVisibility(View.INVISIBLE);
                level_4.setVisibility(View.INVISIBLE);
                level_5.setVisibility(View.INVISIBLE);
                level_6.setVisibility(View.INVISIBLE);
                level_7.setVisibility(View.INVISIBLE);
                level_8.setVisibility(View.INVISIBLE);
                level_9.setVisibility(View.INVISIBLE);
                level_10.setVisibility(View.INVISIBLE);
            }

            mHandlerStart.removeCallbacks(r);
            mHandlerStart.post(r);
            //mHandlerStart.postDelayed(r,50);
        }
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        this.dismiss();
    }

}*/
