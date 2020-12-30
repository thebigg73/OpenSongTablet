package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
    public void onAttach(@NonNull Context context) {
        mListener = (MyInterface) context;
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

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
        View V = inflater.inflate(R.layout.popup_song_details, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(StaticVariables.songfilename);
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe,getContext());
            closeMe.setEnabled(false);
            dismiss();
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        ProcessSong processSong = new ProcessSong();
        Preferences preferences = new Preferences();

        Button editSongDetails = V.findViewById(R.id.editSongDetails);
        editSongDetails.setOnClickListener(v -> {
            mListener.doEdit();
            dismiss();
        });
        TextView t_mAuthor = V.findViewById(R.id.t_mAuthor);
        TextView t_mKey = V.findViewById(R.id.t_mKey);
        TextView t_mCopyright = V.findViewById(R.id.t_mCopyright);
        TextView t_mPresentation = V.findViewById(R.id.t_mPresentation);
        TextView t_mHymnNumber = V.findViewById(R.id.t_mHymnNumber);
        TextView t_mCCLI = V.findViewById(R.id.t_mCCLI);
        TextView t_mNotes = V.findViewById(R.id.t_mNotes);
        TextView v_mTitle = V.findViewById(R.id.v_mTitle);
        TextView v_mAuthor = V.findViewById(R.id.v_mAuthor);
        TextView v_mKey = V.findViewById(R.id.v_mKey);
        TextView v_mCopyright = V.findViewById(R.id.v_mCopyright);
        TextView v_mPresentation = V.findViewById(R.id.v_mPresentation);
        TextView v_mHymnNumber = V.findViewById(R.id.v_mHymnNumber);
        TextView v_mCCLI = V.findViewById(R.id.v_mCCLI);
        TextView v_mNotes = V.findViewById(R.id.v_mNotes);
        TextView v_mLyrics = V.findViewById(R.id.v_mLyrics);

        String k = processSong.getSongKey();
        // Fix the key text
        k = k.replace("(","");
        k = k.replace(")","");

        // Get the capo key if it exitst
        String ck = processSong.getCapoInfo(getContext(), preferences);
        if (!ck.equals("")) {
            ck = " (" + getString(R.string.edit_song_capo) + " " + ck + ")";
            k += ck;
        }

        // Decide what should or should be shown
        v_mTitle.setText(StaticVariables.mTitle);
        setContentInfo(t_mAuthor,v_mAuthor, StaticVariables.mAuthor);
        setContentInfo(t_mKey,v_mKey, k);
        setContentInfo(t_mCopyright,v_mCopyright, StaticVariables.mCopyright);
        setContentInfo(t_mCCLI,v_mCCLI, StaticVariables.mCCLI);
        setContentInfo(t_mPresentation,v_mPresentation, StaticVariables.mPresentation);
        setContentInfo(t_mHymnNumber,v_mHymnNumber, StaticVariables.mHymnNumber);
        setContentInfo(t_mNotes,v_mNotes, StaticVariables.mNotes);

        v_mLyrics.setTypeface(StaticVariables.typefaceLyrics);
        v_mLyrics.setTextSize(8.0f);
        v_mLyrics.setText(StaticVariables.mLyrics);

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);
        return V;
    }

    private void setContentInfo(TextView tv_t, TextView tv_v, String s) {
        if (s!=null && !s.equals("")) {
            tv_t.setVisibility(View.VISIBLE);
            tv_v.setVisibility(View.VISIBLE);
            tv_v.setText(s);
        } else {
            tv_t.setVisibility(View.GONE);
            tv_v.setVisibility(View.GONE);
        }
    }
    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        this.dismiss();
    }

}