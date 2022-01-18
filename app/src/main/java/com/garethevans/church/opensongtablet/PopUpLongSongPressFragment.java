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
        void prepareSongMenu();
    }

    private MyInterface mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (MyInterface) context;
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    private Preferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    static void addtoSet(Context c, Preferences preferences) {

        // If the song is in .pro, .onsong, .txt format, tell the user to convert it first
        // This is done by viewing it (avoids issues with file extension renames)
        // Just in case users running older than lollipop, we don't want to open the file
        // In this case, store the current song as a string so we can go back to it
        if (StaticVariables.songfilename.toLowerCase(StaticVariables.locale).endsWith(".pro") ||
                StaticVariables.songfilename.toLowerCase(StaticVariables.locale).endsWith(".chopro") ||
                StaticVariables.songfilename.toLowerCase(StaticVariables.locale).endsWith(".cho") ||
                StaticVariables.songfilename.toLowerCase(StaticVariables.locale).endsWith(".chordpro") ||
                StaticVariables.songfilename.toLowerCase(StaticVariables.locale).endsWith(".onsong") ||
                StaticVariables.songfilename.toLowerCase(StaticVariables.locale).endsWith(".txt")) {

            // Don't add song yet, but tell the user
            StaticVariables.myToastMessage = c.getResources().getString(R.string.convert_song);
            ShowToast.showToast(c);

        } else if (StaticVariables.songfilename.toLowerCase(StaticVariables.locale).endsWith(".doc") ||
                StaticVariables.songfilename.toLowerCase(StaticVariables.locale).endsWith(".docx")) {
            // Don't add song yet, but tell the user it is unsupported
            StaticVariables.myToastMessage = c.getResources().getString(R.string.file_type_unknown);
            ShowToast.showToast(c);

        } else {
            if (preferences.getMyPreferenceBoolean(c,"ccliAutomaticLogging",false)) {
                // Now we need to get the song info quickly to log it correctly
                // as this might not be the song loaded
                String[] vals = LoadXML.getCCLILogInfo(c, preferences, StaticVariables.whichSongFolder, StaticVariables.songfilename);
                if (vals.length == 4 && vals[0] != null && vals[1] != null && vals[2] != null && vals[3] != null) {
                    PopUpCCLIFragment.addUsageEntryToLog(c, preferences, StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename,
                            vals[0], vals[1], vals[2], vals[3], "6"); // Printed
                }
            }

            // Add to end of set
            String val = preferences.getMyPreferenceString(c,"setCurrent","") + StaticVariables.whatsongforsetwork;
            preferences.setMyPreferenceString(c,"setCurrent",val);
            // Tell the user that the song has been added.
            // For a received song (which is about to become a variation) use the stored received song filename
            if (StaticVariables.songfilename.equals("ReceivedSong")) {
                StaticVariables.myToastMessage = "\"" + StaticVariables.receivedSongfilename + "\" " +
                        c.getResources().getString(R.string.addedtoset);
            } else {
                StaticVariables.myToastMessage = "\"" + StaticVariables.songfilename + "\" " +
                        c.getResources().getString(R.string.addedtoset);
            }
            ShowToast.showToast(c);
        }
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        this.dismiss();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog()!=null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(true);
        }

        View V = inflater.inflate(R.layout.popup_longsongpress, container, false);

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

        preferences = new Preferences();

        // Vibrate to let the user know something happened
        DoVibrate.vibrate(requireActivity(),50);

        // Initialise the views
        Button addSongToSet_Button = V.findViewById(R.id.addSongToSet_Button);
        Button deleteSong_Button = V.findViewById(R.id.deleteSong_Button);
        Button renameSong_Button = V.findViewById(R.id.renameSong_Button);
        Button duplicateSong_Button = V.findViewById(R.id.duplicateSong_Button);
        Button shareSong_Button = V.findViewById(R.id.shareSong_Button);
        Button editSong_Button = V.findViewById(R.id.editSong_Button);

        // Set up listeners for the buttons
        addSongToSet_Button.setOnClickListener(view -> {
            addtoSet(getContext(), preferences);
            if (mListener!=null) {
                mListener.songLongClick();
            }
            dismiss();
        });
        deleteSong_Button.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "deletesong";
            if (mListener!=null) {
                mListener.openFragment();
            }
            dismiss();
        });
        shareSong_Button.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "exportsong";
            if (mListener!=null) {
                mListener.shareSong();
            }
            dismiss();
        });
        renameSong_Button.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "renamesong";
            if (mListener!=null) {
                mListener.openFragment();
            }
            dismiss();
        });
        duplicateSong_Button.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "duplicate";
            if (mListener!=null) {
                mListener.openFragment();
            }
            dismiss();
        });
        editSong_Button.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "editsong";
            if (mListener!=null) {
                mListener.openFragment();
            }
            dismiss();
        });

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }
    @Override
    public void onDismiss(@NonNull final DialogInterface dialog) {
        // IV - Update the song menu
        if (mListener!=null) {
            mListener.prepareSongMenu();
        }
    }
}