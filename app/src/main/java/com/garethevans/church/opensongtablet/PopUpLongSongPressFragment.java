package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class PopUpLongSongPressFragment extends DialogFragment {

    static PopUpLongSongPressFragment newInstance() {
        PopUpLongSongPressFragment frag;
        frag = new PopUpLongSongPressFragment();
        return frag;
    }

    public interface MyInterface {
        void openFragment();
        void shareSong();
        void songLongClick();
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

    Button addSongToSet_Button;
    Button deleteSong_Button;
    Button renameSong_Button;
    Button shareSong_Button;
    Button cancelSong_Button;

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
        getDialog().setTitle(FullscreenActivity.songfilename);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_longsongpress, container, false);

        // Vibreate to let the user know something happened
        DoVibrate.vibrate(getActivity(),50);

        // Initialise the views
        addSongToSet_Button = (Button) V.findViewById(R.id.addSongToSet_Button);
        deleteSong_Button = (Button) V.findViewById(R.id.deleteSong_Button);
        renameSong_Button = (Button) V.findViewById(R.id.renameSong_Button);
        shareSong_Button = (Button) V.findViewById(R.id.shareSong_Button);
        cancelSong_Button = (Button) V.findViewById(R.id.cancelSong_Button);


        // Set up listeners for the buttons
        addSongToSet_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addtoSet();
                mListener.songLongClick();
                dismiss();
            }
        });
        deleteSong_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "deletesong";
                dismiss();
                mListener.openFragment();
            }
        });
        shareSong_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "exportsong";
                dismiss();
                mListener.shareSong();
            }
        });
        renameSong_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "renamesong";
                dismiss();
                mListener.openFragment();
            }
        });
        cancelSong_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return V;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

    public void addtoSet() {
        FullscreenActivity.addingtoset = true;

        // If the song is in .pro, .onsong, .txt format, tell the user to convert it first
        // This is done by viewing it (avoids issues with file extension renames)
        // Just in case users running older than lollipop, we don't want to open the file
        // In this case, store the current song as a string so we can go back to it
        if (FullscreenActivity.songfilename.toLowerCase(FullscreenActivity.locale).endsWith(".pro") ||
                FullscreenActivity.songfilename.toLowerCase(FullscreenActivity.locale).endsWith(".chopro") ||
                FullscreenActivity.songfilename.toLowerCase(FullscreenActivity.locale).endsWith(".cho") ||
                FullscreenActivity.songfilename.toLowerCase(FullscreenActivity.locale).endsWith(".chordpro") ||
                FullscreenActivity.songfilename.toLowerCase(FullscreenActivity.locale).endsWith(".onsong") ||
                FullscreenActivity.songfilename.toLowerCase(FullscreenActivity.locale).endsWith(".txt")) {

            // Don't add song yet, but tell the user
            FullscreenActivity.myToastMessage = getActivity().getResources().getString(R.string.convert_song);
            ShowToast.showToast(getActivity());

        } else if (FullscreenActivity.songfilename.toLowerCase(FullscreenActivity.locale).endsWith(".doc") ||
                FullscreenActivity.songfilename.toLowerCase(FullscreenActivity.locale).endsWith(".docx")) {
            // Don't add song yet, but tell the user it is unsupported
            FullscreenActivity.myToastMessage = getActivity().getResources().getString(R.string.file_type_unknown);
            ShowToast.showToast(getActivity());

        } else {
            // Set the appropriate song filename
            if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
                FullscreenActivity.whatsongforsetwork = "$**_" + FullscreenActivity.songfilename + "_**$";
            } else {
                FullscreenActivity.whatsongforsetwork = "$**_" + FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.songfilename + "_**$";
            }

            // Allow the song to be added, even if it is already there
            FullscreenActivity.mySet = FullscreenActivity.mySet + FullscreenActivity.whatsongforsetwork;

            // Tell the user that the song has been added.
            FullscreenActivity.myToastMessage = "\"" + FullscreenActivity.songfilename + "\" " + getActivity().getResources().getString(R.string.addedtoset);
            ShowToast.showToast(getActivity());

            // Save the set and other preferences
            Preferences.savePreferences();
        }
    }
}