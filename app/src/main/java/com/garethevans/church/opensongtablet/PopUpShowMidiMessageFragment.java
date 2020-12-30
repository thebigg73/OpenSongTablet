package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class PopUpShowMidiMessageFragment extends DialogFragment {

    private Midi m;
    private ArrayList<String> songMidiMessages;
    private ArrayAdapter<String> midiMessagesAdapter;

    static PopUpShowMidiMessageFragment newInstance() {
        PopUpShowMidiMessageFragment frag;
        frag = new PopUpShowMidiMessageFragment();
        return frag;
    }

    public interface MyInterface {
        void sendMidi();
        void openMyDrawers(String which);
        void openFragment();
        void prepareOptionMenu();
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.dismiss();
        }
        if (getDialog()!=null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(true);
        }

        View V = inflater.inflate(R.layout.popup_showmidimessages, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getString(R.string.midi));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe, getContext());
            closeMe.setEnabled(false);
            dismiss();
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        // Initialise the Midi - this is only available for Marshmallow+
        m = new Midi();

        Preferences preferences = new Preferences();

        // Initialise the views
        ListView songMessages = V.findViewById(R.id.songMessages);
        Button sendAll = V.findViewById(R.id.sendAll);
        Button editMidi = V.findViewById(R.id.editMidi);

        // Initialise the lists
        initialiseArrays();
        songMessages.setAdapter(midiMessagesAdapter);

        // Set listeners
        songMessages.setOnItemClickListener((adapterView, view, i, l) -> sendOneMessage(songMidiMessages.get(i)));

        sendAll.setOnClickListener(view -> doSendAll());

        editMidi.setOnClickListener(view -> openMidiEdit());
        PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog(), preferences);

        return V;
    }

    private void doSendAll() {
        // Trigger the Override in StageMode/PresenterMode to send all the Midi messages stored
        if (mListener!=null) {
            mListener.sendMidi();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void sendOneMessage(String message) {
        boolean success = false;
        if (m==null) {
            m = new Midi();
        }
        try {
            success = m.sendMidi(m.returnBytesFromHexText(message));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!success) {
            StaticVariables.myToastMessage = getString(R.string.midi_error);
            ShowToast.showToast(getContext());
        }
    }

    private void openMidiEdit() {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            if (mListener != null) {
                StaticVariables.whichOptionMenu = "MIDI";
                mListener.prepareOptionMenu();
                mListener.openMyDrawers("option");
                FullscreenActivity.whattodo = "midicommands";
                mListener.openFragment();
            }
        } else {
            StaticVariables.myToastMessage = getString(R.string.nothighenoughapi);
            ShowToast.showToast(getContext());
        }
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initialiseArrays() {
        songMidiMessages = new ArrayList<>();
        ArrayList<String> songMidiMessagesNice = new ArrayList<>();
        if (m==null) {
            m = new Midi();
        }
        // Add what is there already
        String[] bits = StaticVariables.mMidi.trim().split("\n");
        try {
            for (String s : bits) {
                if (s!=null && !s.equals("") && !s.isEmpty() && getContext()!=null) {
                    // Get a human readable version of the midi code
                    String hr = m.getReadableStringFromHex(s,getContext());
                    String message = hr + "\n" + "(" + s + ")";
                    songMidiMessagesNice.add(message);
                    songMidiMessages.add(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        midiMessagesAdapter = new ArrayAdapter<>(requireContext(),R.layout.my_spinner, songMidiMessagesNice);
    }
}
