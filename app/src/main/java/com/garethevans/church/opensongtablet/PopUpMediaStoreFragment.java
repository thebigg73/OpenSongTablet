package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.DialogFragment;
import androidx.loader.content.CursorLoader;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.Objects;

public class PopUpMediaStoreFragment extends DialogFragment {

    // TODO
    // Will replace this with built in file chooser

    private ListView mediaStore_ListView;
    @SuppressLint("StaticFieldLeak")
    private static FloatingActionButton startPlay;
    private TextView mediaSelected;
    private SeekBar scrubbar_SeekBar;
    private TextView scrubbar_TextView;
    private int mptotaltimesecs = 0;
    private String[] from;
    private int[] to;
    private Handler seekHandler;

    static PopUpMediaStoreFragment newInstance() {
        PopUpMediaStoreFragment frag;
        frag = new PopUpMediaStoreFragment();
        return frag;
    }

    private final Uri sourceUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        View V = inflater.inflate(R.layout.popup_mediastore, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.media_chooser));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe,getActivity());
            closeMe.setEnabled(false);
            dismiss();
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        Preferences preferences = new Preferences();

        from = new String[] {MediaStore.MediaColumns.TITLE};
        to = new int[] {android.R.id.text1};

        scrubbar_SeekBar = V.findViewById(R.id.scrubbar_SeekBar);
        scrubbar_TextView = V.findViewById(R.id.scrubbar_TextView);
        mediaStore_ListView = V.findViewById(R.id.mediaStore_ListView);
        mediaSelected = V.findViewById(R.id.mediaSelected);
        mediaSelected.setText(PresenterMode.mpTitle);
        SwitchCompat externalSwitch = V.findViewById(R.id.externalSwitch);
        externalSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> updateMedia());
        startPlay = V.findViewById(R.id.startPlay);
        startPlay.setOnClickListener(v -> startPlay());

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
                    startPlay.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_stop_white_36dp,null));
                    seekHandler = new Handler();
                    seekHandler.post(run);
                }
            } catch (Exception e) {
                // Ooops
            }
        }
        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    private final Runnable run = this::seekUpdate;

    private void seekUpdate() {
        if (PresenterMode.mp!=null && PresenterMode.mp.isPlaying()) {
            int pos = (int) ((float)PresenterMode.mp.getCurrentPosition()/1000.0f);
            int dur = (int) ((float)PresenterMode.mp.getDuration()/1000.0f);
            scrubbar_SeekBar.setProgress(pos);
            String currentposition = TimeTools.timeFormatFixer(pos) + " / " + TimeTools.timeFormatFixer(dur);
            scrubbar_TextView.setText(currentposition);
            seekHandler.postDelayed(run, 1000);
        }
    }

    private void updateMedia() {

        /*if (FullscreenActivity.mediaStore.equals("ext")) {
            sourceUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        } else {
            sourceUri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
        }*/

        CursorLoader cursorLoader = new CursorLoader(
                Objects.requireNonNull(getActivity()),
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

        mediaStore_ListView.setOnItemClickListener((parent, view, position, id) -> {
            Cursor cursor1 = ((SimpleCursorAdapter) mediaStore_ListView.getAdapter()).getCursor();
            cursor1.moveToPosition(position);
            String fullname = cursor1.getString(cursor1.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String data = cursor1.getString(cursor1.getColumnIndex(MediaStore.Audio.Media.DATA));
            mediaSelected.setText(fullname);
            startPlay.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_play_white_36dp,null));
            PresenterMode.mpTitle = fullname;
            if (PresenterMode.mp.isPlaying()) {
                PresenterMode.mp.stop();
            }
            PresenterMode.mp.reset();
            try {
                PresenterMode.mp.setDataSource(data);
                PresenterMode.mp.prepareAsync();
                PresenterMode.mp.setOnPreparedListener(mediaPlayer -> {
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
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void startPlay() {
        if (PresenterMode.mp.isPlaying()) {
            if (seekHandler!=null) {
                seekHandler.removeCallbacks(run);
            }
            seekHandler = null;
            // Stop the media player
            PresenterMode.mp.pause();
            startPlay.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_play_white_36dp,null));
        } else {
            if (!mediaSelected.getText().toString().equals("")) {
                if (seekHandler!=null) {
                    seekHandler.removeCallbacks(run);
                }
                seekHandler = new Handler();
                seekHandler.postDelayed(run,1000);
                PresenterMode.mp.start();
                startPlay.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_stop_white_36dp,null));
            }
        }
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        this.dismiss();
    }

}
