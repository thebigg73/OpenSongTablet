package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
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
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_song_details, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(FullscreenActivity.songfilename);
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setVisibility(View.GONE);

        Button editSongDetails = V.findViewById(R.id.editSongDetails);
        editSongDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.doEdit();
                dismiss();
            }
        });
        TextView v_mTitle = V.findViewById(R.id.v_mTitle);
        TextView v_mAuthor = V.findViewById(R.id.v_mAuthor);
        TextView v_mCopyright = V.findViewById(R.id.v_mCopyright);
        TextView v_mPresentation = V.findViewById(R.id.v_mPresentation);
        TextView v_mHymnNumber = V.findViewById(R.id.v_mHymnNumber);
        TextView v_mCCLI = V.findViewById(R.id.v_mCCLI);
        TextView v_mNotes = V.findViewById(R.id.v_mNotes);
        TextView v_mLyrics = V.findViewById(R.id.v_mLyrics);

        v_mTitle.setText(FullscreenActivity.mTitle);
        v_mAuthor.setText(FullscreenActivity.mAuthor);
        v_mCopyright.setText(FullscreenActivity.mCopyright);
        v_mCCLI.setText(FullscreenActivity.mCCLI);
        v_mPresentation.setText(FullscreenActivity.mPresentation);
        v_mHymnNumber.setText(FullscreenActivity.mHymnNumber);
        v_mNotes.setText(FullscreenActivity.mNotes);
        v_mLyrics.setTypeface(FullscreenActivity.typeface1);
        v_mLyrics.setTextSize(8.0f);
        v_mLyrics.setText(FullscreenActivity.mLyrics);

        return V;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}