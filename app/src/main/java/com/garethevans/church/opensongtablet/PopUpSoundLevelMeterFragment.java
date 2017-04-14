package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class PopUpSoundLevelMeterFragment extends DialogFragment {

    private AudioRecord audio;
    int bufferSize;

    static PopUpSoundLevelMeterFragment newInstance() {
        PopUpSoundLevelMeterFragment frag;
        frag = new PopUpSoundLevelMeterFragment();
        return frag;
    }

    TextView dBTextView;
    Handler mHandlerStart;
    ImageView level_1;
    ImageView level_2;
    ImageView level_3;
    ImageView level_4;
    ImageView level_5;
    ImageView level_6;
    ImageView level_7;
    ImageView level_8;
    ImageView level_9;
    ImageView level_10;
    SeekBar maxvolrange;
    TextView volval;
    TextView averagevol;
    Button resetaverage;
    int totalvols = 0;
    int counts = 0;
    float avvol = 0.0f;

    Runnable r = new Runnable() {
        @Override
        public void run() {
            sampleSound();
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
        }
        if (getDialog().getWindow()!=null) {
            getDialog().getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.popup_dialogtitle);
            TextView title = (TextView) getDialog().getWindow().findViewById(R.id.dialogtitle);
            title.setText(getActivity().getResources().getString(R.string.volume));
            FloatingActionButton closeMe = (FloatingActionButton) getDialog().getWindow().findViewById(R.id.closeMe);
            closeMe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
            FloatingActionButton saveMe = (FloatingActionButton) getDialog().getWindow().findViewById(R.id.saveMe);
            saveMe.setVisibility(View.GONE);
        } else {
            getDialog().setTitle(getActivity().getResources().getString(R.string.volume));
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
        getDialog().requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_soundlevelmeter, container, false);
        dBTextView = (TextView) V.findViewById(R.id.dBTextView);

        level_1 = (ImageView) V.findViewById(R.id.level_1);
        level_2 = (ImageView) V.findViewById(R.id.level_2);
        level_3 = (ImageView) V.findViewById(R.id.level_3);
        level_4 = (ImageView) V.findViewById(R.id.level_4);
        level_5 = (ImageView) V.findViewById(R.id.level_5);
        level_6 = (ImageView) V.findViewById(R.id.level_6);
        level_7 = (ImageView) V.findViewById(R.id.level_7);
        level_8 = (ImageView) V.findViewById(R.id.level_8);
        level_9 = (ImageView) V.findViewById(R.id.level_9);
        level_10 = (ImageView) V.findViewById(R.id.level_10);

        averagevol = (TextView) V.findViewById(R.id.averagevol);
        resetaverage = (Button) V.findViewById(R.id.resetaverage);

        resetaverage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                totalvols = 0;
                counts = 0;
                avvol = 0.0f;
            }
        });

        volval = (TextView) V.findViewById(R.id.volval);

        maxvolrange = (SeekBar) V.findViewById(R.id.maxvolrange);
        maxvolrange.setMax(7);
        int myprogress;
        String mytext;
        switch (FullscreenActivity.maxvolrange) {
            case 50:
                myprogress = 0;
                mytext = "0 - 50";
                break;

            case 100:
                myprogress = 1;
                mytext = "0 - 100";
                break;

            case 200:
                myprogress = 2;
                mytext = "0 - 200";
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

            default:
                myprogress = 2;
                mytext = "0 - 200";
                break;
        }
        maxvolrange.setProgress(myprogress);
        volval.setText(mytext);

        maxvolrange.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //volval.setText(progress);
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

                FullscreenActivity.maxvolrange = volrangechosen;
                volval.setText(text);
                Preferences.savePreferences();
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

        return V;
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
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

    public void sampleSound() {

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

            if (vol>(0.9*(float)FullscreenActivity.maxvolrange)) {
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
            } else if (vol>(0.8*(float)FullscreenActivity.maxvolrange)) {
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
            } else if (vol>(0.7*(float)FullscreenActivity.maxvolrange)) {
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
            } else if (vol>(0.6*(float)FullscreenActivity.maxvolrange)) {
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
            } else if (vol>(0.5*(float)FullscreenActivity.maxvolrange)) {
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
            } else if (vol>(0.4*(float)FullscreenActivity.maxvolrange)) {
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
            } else if (vol>(0.3*(float)FullscreenActivity.maxvolrange)) {
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
            } else if (vol>(0.2*(float)FullscreenActivity.maxvolrange)) {
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
            } else if (vol>(0.1*(float)FullscreenActivity.maxvolrange)) {
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
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}
