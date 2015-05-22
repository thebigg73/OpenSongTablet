package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class PopUpSongDetailsFragment extends DialogFragment {

    static PopUpSongDetailsFragment newInstance() {
        PopUpSongDetailsFragment frag;
        frag = new PopUpSongDetailsFragment();
        return frag;
    }

    public interface MyInterface {
        void doEdit();
    }

    private MyInterface mListener;

        @Override
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
        getDialog().setTitle(FullscreenActivity.songfilename);
        View V = inflater.inflate(R.layout.popup_song_details, container, false);

        Button closeFragDialog = (Button) V.findViewById(R.id.closeSongDetails);
        closeFragDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        Button editSongDetails = (Button) V.findViewById(R.id.editSongDetails);
        editSongDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.doEdit();
                dismiss();
            }
        });
        TextView v_mTitle = (TextView) V.findViewById(R.id.v_mTitle);
        TextView v_mAuthor = (TextView) V.findViewById(R.id.v_mAuthor);
        TextView v_mCopyright = (TextView) V.findViewById(R.id.v_mCopyright);
        TextView v_mPresentation = (TextView) V.findViewById(R.id.v_mPresentation);
        TextView v_mHymnNumber = (TextView) V.findViewById(R.id.v_mHymnNumber);
        TextView v_mCCLI = (TextView) V.findViewById(R.id.v_mCCLI);
        TextView v_mNotes = (TextView) V.findViewById(R.id.v_mNotes);
        TextView v_mLyrics = (TextView) V.findViewById(R.id.v_mLyrics);

        v_mTitle.setText(FullscreenActivity.mTitle);
        v_mAuthor.setText(FullscreenActivity.mAuthor);
        v_mCopyright.setText(FullscreenActivity.mCopyright);
        v_mCCLI.setText(FullscreenActivity.mCCLI);
        v_mPresentation.setText(FullscreenActivity.mPresentation);
        v_mHymnNumber.setText(FullscreenActivity.mHymnNumber);
        v_mNotes.setText(FullscreenActivity.mNotes);
        v_mLyrics.setText(FullscreenActivity.mLyrics);

        return V;
    }
}