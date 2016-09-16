package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class PopUpOptionMenuSong extends DialogFragment {

    static PopUpOptionMenuSong newInstance() {
        PopUpOptionMenuSong frag;
        frag = new PopUpOptionMenuSong();
        return frag;
    }

    public interface MyInterface {
        void openFragment();
        void loadSong();
        void shareSong();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View V = inflater.inflate(R.layout.popup_option_song, container, false);

        // List the views
        Button songEditButton = (Button) V.findViewById(R.id.setLoadButton);
        Button songStickyButton = (Button) V.findViewById(R.id.songStickyButton);
        Button songRenameButton = (Button) V.findViewById(R.id.songRenameButton);
        Button songNewButton = (Button) V.findViewById(R.id.songNewButton);
        Button songDeleteButton = (Button) V.findViewById(R.id.songDeleteButton);
        Button songExportButton = (Button) V.findViewById(R.id.songExportButton);
        Button songPresentationOrderButton = (Button) V.findViewById(R.id.songPresentationOrderButton);

        // Set the text as to uppercase as per locale
        songEditButton.setText(getActivity().getString(R.string.options_set_load).toUpperCase(FullscreenActivity.locale));
        songStickyButton.setText(getActivity().getString(R.string.options_song_stickynotes).toUpperCase(FullscreenActivity.locale));
        songRenameButton.setText(getActivity().getString(R.string.options_song_rename).toUpperCase(FullscreenActivity.locale));
        songNewButton.setText(getActivity().getString(R.string.options_song_new).toUpperCase(FullscreenActivity.locale));
        songDeleteButton.setText(getActivity().getString(R.string.options_song_delete).toUpperCase(FullscreenActivity.locale));
        songExportButton.setText(getActivity().getString(R.string.options_song_export).toUpperCase(FullscreenActivity.locale));
        songPresentationOrderButton.setText(getActivity().getString(R.string.edit_song_presentation).toUpperCase(FullscreenActivity.locale));

        // Set the button listeners
        songEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "editsong";
                mListener.openFragment();
                dismiss();
            }
        });
        songStickyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "editnotes";
                mListener.openFragment();
                dismiss();
            }
        });
        songRenameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "renamesong";
                mListener.openFragment();
                dismiss();
            }
        });
        songNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "createsong";
                mListener.openFragment();
                dismiss();
            }
        });
        songDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "deletesong";
                mListener.openFragment();
                dismiss();
            }
        });
        songExportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "sharesong";
                mListener.shareSong();
                dismiss();
            }
        });
        songPresentationOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "createsong";
                mListener.openFragment();
                dismiss();
            }
        });

        return V;
    }
}