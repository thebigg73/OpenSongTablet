package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import java.io.IOException;

public class PopUpEditStickyFragment extends DialogFragment {

    static PopUpEditStickyFragment newInstance() {
        PopUpEditStickyFragment frag;
        frag = new PopUpEditStickyFragment();
        return frag;
    }

    public interface MyInterface {
        void stickyNotesUpdate();
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
        getDialog().setTitle(getActivity().getResources().getString(R.string.options_song_stickynotes));
        View V = inflater.inflate(R.layout.popup_editsticky, container, false);

        // Initialise the views
        final EditText editStickyText = (EditText) V.findViewById(R.id.editStickyText);
        Button editStickySave = (Button) V.findViewById(R.id.editStickySave);
        Button editStickCancel = (Button) V.findViewById(R.id.editStickyCancel);

        // Set the text if it exists
        editStickyText.setText(FullscreenActivity.mNotes);

        // Listen for the buttons
        editStickCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        editStickySave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullscreenActivity.mNotes = editStickyText.getText().toString();
                // Save the file
                EditSong.prepareSongXML();
                try {
                    EditSong.justSaveSongXML();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dismiss();
                mListener.stickyNotesUpdate();
            }
        });
        return V;
    }
}
