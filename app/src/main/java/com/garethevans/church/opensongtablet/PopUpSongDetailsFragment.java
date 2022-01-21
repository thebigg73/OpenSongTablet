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
        // IV - If ReceivedSong then, if available, use the stored received song filename
        if (StaticVariables.songfilename.equals("ReceivedSong") && !StaticVariables.receivedSongfilename.equals("")) {
            title.setText("ReceivedSong: " + StaticVariables.receivedSongfilename);
        } else {
            title.setText(StaticVariables.songfilename);
        }
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
        TextView t_mPresentation = V.findViewById(R.id.t_mPresentation);
        TextView t_mHymnNumber = V.findViewById(R.id.t_mHymnNumber);
        TextView t_mCCLI = V.findViewById(R.id.t_mCCLI);
        TextView t_mNotes = V.findViewById(R.id.t_mNotes);
        TextView t_mLyrics = V.findViewById(R.id.t_mLyrics);
        TextView v_mTitle = V.findViewById(R.id.v_mTitle);
        TextView v_mAuthor = V.findViewById(R.id.v_mAuthor);
        TextView v_mKey = V.findViewById(R.id.v_mKey);
        TextView v_mCopyright = V.findViewById(R.id.v_mCopyright);
        TextView v_mPresentation = V.findViewById(R.id.v_mPresentation);
        TextView v_mHymnNumber = V.findViewById(R.id.v_mHymnNumber);
        TextView v_mCCLI = V.findViewById(R.id.v_mCCLI);
        TextView v_mNotes = V.findViewById(R.id.v_mNotes);
        TextView v_mLyrics = V.findViewById(R.id.v_mLyrics);

        // IV - Try to generate a capo/key/tempo/time line
        StringBuilder songInformation = new StringBuilder();
        String sprefix = "";

        if (!StaticVariables.mCapo.equals("") && !StaticVariables.mCapo.equals("0")) {
            // If we are using a capo, add the capo display

            songInformation.append(sprefix).append("Capo: ");
            sprefix = " | ";
            int mcapo;
            try {
                mcapo = Integer.parseInt("0" + StaticVariables.mCapo);
            } catch (Exception e) {
                mcapo = -1;
            }
            if ((mcapo > 0) && (preferences.getMyPreferenceBoolean(getContext(), "capoInfoAsNumerals", false))) {
                songInformation.append(numberToNumeral(mcapo));
            } else {
                songInformation.append("").append(mcapo);
            }

            Transpose transpose = new Transpose();
            if (!StaticVariables.mKey.equals("")) {
                songInformation.append(" (").append(transpose.capoTranspose(getContext(), preferences, StaticVariables.mKey)).append(")");
            }
        }

        if (!StaticVariables.mKey.equals("")) {
            songInformation.append(sprefix).append(getContext().getResources().getString(R.string.edit_song_key)).append(": ").append(StaticVariables.mKey);
            sprefix = " | ";
        }
        if (!StaticVariables.mTempo.equals("")) {
            songInformation.append(sprefix).append(getContext().getResources().getString(R.string.edit_song_tempo)).append(": ").append(StaticVariables.mTempo);
            sprefix = " | ";
        }
        if (!StaticVariables.mTimeSig.equals("")) {
            songInformation.append(sprefix).append(getContext().getResources().getString(R.string.edit_song_timesig)).append(": ").append(StaticVariables.mTimeSig);
            sprefix = " | ";
        }

        // Decide what should or should be shown
        v_mTitle.setText(StaticVariables.mTitle);
        setContentInfo(null,v_mAuthor, StaticVariables.mAuthor);
        setContentInfo(null,v_mCopyright, StaticVariables.mCopyright);
        setContentInfo(null,v_mKey, songInformation.toString());
        setContentInfo(t_mCCLI,v_mCCLI, StaticVariables.mCCLI);
        setContentInfo(t_mPresentation,v_mPresentation, StaticVariables.mPresentation);
        setContentInfo(t_mHymnNumber,v_mHymnNumber, StaticVariables.mHymnNumber);
        setContentInfo(t_mNotes,v_mNotes, StaticVariables.mNotes);

        v_mLyrics.setTypeface(StaticVariables.typefaceLyrics);
        v_mLyrics.setTextSize(8.0f);

        // IV - No Lyrics for PDF and Image songs
        if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
            StaticVariables.mLyrics = "";
        }

        setContentInfo(t_mLyrics, v_mLyrics, StaticVariables.mLyrics);

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);
        return V;
    }

    private void setContentInfo(TextView tv_t, TextView tv_v, String s) {
        if (s!=null && !s.equals("")) {
            if (tv_t != null) {
                tv_t.setVisibility(View.VISIBLE);
            }
            tv_v.setVisibility(View.VISIBLE);
            tv_v.setText(s);
        } else {
            if (tv_t != null) {
                tv_t.setVisibility(View.GONE);
            }
            tv_v.setVisibility(View.GONE);
        }
    }
    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        this.dismiss();
    }

    private String numberToNumeral(int num) {
        String s;
        switch (num) {
            default:
                s = "";
                break;
            case 1:
                s = "I";
                break;
            case 2:
                s = "II";
                break;
            case 3:
                s = "III";
                break;
            case 4:
                s = "IV";
                break;
            case 5:
                s = "V";
                break;
            case 6:
                s = "VI";
                break;
            case 7:
                s = "VII";
                break;
            case 8:
                s = "VIII";
                break;
            case 9:
                s = "IX";
                break;
            case 10:
                s = "X";
                break;
            case 11:
                s = "XI";
                break;
            case 12:
                s = "XII";
                break;
        }
        return s;
    }

}