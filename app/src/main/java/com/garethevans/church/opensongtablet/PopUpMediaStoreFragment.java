package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;

public class PopUpMediaStoreFragment extends DialogFragment {

    ListView mediaStore_ListView;
    @SuppressLint("StaticFieldLeak")
    static FloatingActionButton startPlay;
    SwitchCompat externalSwitch;
    TextView mediaSelected;
    SeekBar scrubbar_SeekBar;
    TextView scrubbar_TextView;
    int mptotaltimesecs = 0;
    String[] from;
    int[] to;
    Handler seekHandler;

    static PopUpMediaStoreFragment newInstance() {
        PopUpMediaStoreFragment frag;
        frag = new PopUpMediaStoreFragment();
        return frag;
    }

    Uri sourceUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // safety check
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        View V = inflater.inflate(R.layout.popup_mediastore, container, false);

        TextView title = (TextView) V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.media_chooser));
        final FloatingActionButton closeMe = (FloatingActionButton) V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        FloatingActionButton saveMe = (FloatingActionButton) V.findViewById(R.id.saveMe);
        saveMe.setVisibility(View.GONE);

        from = new String[] {MediaStore.MediaColumns.TITLE};
        to = new int[] {android.R.id.text1};

        scrubbar_SeekBar = (SeekBar) V.findViewById(R.id.scrubbar_SeekBar);
        scrubbar_TextView = (TextView) V.findViewById(R.id.scrubbar_TextView);
        mediaStore_ListView = (ListView) V.findViewById(R.id.mediaStore_ListView);
        mediaSelected = (TextView) V.findViewById(R.id.mediaSelected);
        mediaSelected.setText(PresenterMode.mpTitle);
        externalSwitch = (SwitchCompat) V.findViewById(R.id.externalSwitch);
        if (FullscreenActivity.mediaStore.equals("ext")) {
            externalSwitch.setChecked(true);
        } else {
            externalSwitch.setChecked(false);
        }
        externalSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    FullscreenActivity.mediaStore = "ext";
                } else {
                    FullscreenActivity.mediaStore = "int";
                }
                Preferences.savePreferences();
                updateMedia();
            }
        });
        startPlay = (FloatingActionButton) V.findViewById(R.id.startPlay);
        startPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPlay();
            }
        });

        updateMedia();

        scrubbar_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(PresenterMode.mp != null && fromUser) {
                    PresenterMode.mp.seekTo(progress * 1000);
                    int duration = (int) ((float) PresenterMode.mp.getDuration() / 1000.0f);
                    String info = TimeTools.timeFormatFixer(progress) + " / " + TimeTools.timeFormatFixer((duration));
                    scrubbar_TextView.setText(info);
                }
            }
        });

        if (PresenterMode.mp!=null) {
            try {
                int duration = (int) ((float) PresenterMode.mp.getDuration() / 1000.0f);
                int position = (int) ((float) PresenterMode.mp.getCurrentPosition() / 1000.0f);
                String info = TimeTools.timeFormatFixer(position) + " / " + TimeTools.timeFormatFixer((duration));
                scrubbar_TextView.setText(info);
                scrubbar_SeekBar.setMax(duration);
                scrubbar_SeekBar.setProgress(position);
                if (PresenterMode.mp.isPlaying()) {
                    startPlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop_white_36dp));
                    seekHandler = new Handler();
                    seekHandler.post(run);
                }
            } catch (Exception e) {
                // Ooops
            }
        }
        return V;
    }

    Runnable run = new Runnable() {
        @Override
        public void run() {
            seekUpdate();
        }
    };

    @SuppressWarnings("deprecation")
    public void seekUpdate() {
        if (PresenterMode.mp!=null && PresenterMode.mp.isPlaying()) {
            int pos = (int) ((float)PresenterMode.mp.getCurrentPosition()/1000.0f);
            int dur = (int) ((float)PresenterMode.mp.getDuration()/1000.0f);
            scrubbar_SeekBar.setProgress(pos);
            String currentposition = TimeTools.timeFormatFixer(pos) + " / " + TimeTools.timeFormatFixer(dur);
            scrubbar_TextView.setText(currentposition);
            seekHandler.postDelayed(run, 1000);
        }
    }

    @SuppressWarnings("deprecation")
    public void updateMedia() {

        if (FullscreenActivity.mediaStore.equals("ext")) {
            sourceUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        } else {
            sourceUri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
        }

        CursorLoader cursorLoader = new CursorLoader(
                getActivity(),
                sourceUri,
                null,
                null,
                null,
                MediaStore.Audio.Media.TITLE);

        Cursor cursor = cursorLoader.loadInBackground();

        ListAdapter adapter = new SimpleCursorAdapter(
                getActivity(),
                android.R.layout.simple_list_item_1,
                cursor,
                from,
                to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        mediaStore_ListView.setAdapter(adapter);

        mediaStore_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = ((SimpleCursorAdapter) mediaStore_ListView.getAdapter()).getCursor();
                cursor.moveToPosition(position);
                String fullname = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                mediaSelected.setText(fullname);
                startPlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_white_36dp));
                PresenterMode.mpTitle = fullname;
                if (PresenterMode.mp.isPlaying()) {
                    PresenterMode.mp.stop();
                }
                PresenterMode.mp.reset();
                try {
                    PresenterMode.mp.setDataSource(data);
                    PresenterMode.mp.prepareAsync();
                    PresenterMode.mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            if (seekHandler!=null) {
                                seekHandler.removeCallbacks(run);
                            }
                            seekHandler = new Handler();
                            mptotaltimesecs = (int) ((float) PresenterMode.mp.getDuration()/1000.0f);
                            scrubbar_SeekBar.setMax(mptotaltimesecs);
                            scrubbar_SeekBar.setProgress(0);
                            String totaltime = TimeTools.timeFormatFixer(mptotaltimesecs);
                            String currentposition = TimeTools.timeFormatFixer(0) + " / " + totaltime;
                            scrubbar_TextView.setText(currentposition);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @SuppressWarnings("deprecation")
    public void startPlay() {
        if (PresenterMode.mp.isPlaying()) {
            if (seekHandler!=null) {
                seekHandler.removeCallbacks(run);
            }
            seekHandler = null;
            // Stop the media player
            PresenterMode.mp.pause();
            startPlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_white_36dp));
        } else {
            if (!mediaSelected.getText().toString().equals("")) {
                if (seekHandler!=null) {
                    seekHandler.removeCallbacks(run);
                }
                seekHandler = new Handler();
                seekHandler.postDelayed(run,1000);
                PresenterMode.mp.start();
                startPlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop_white_36dp));
            }
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}
