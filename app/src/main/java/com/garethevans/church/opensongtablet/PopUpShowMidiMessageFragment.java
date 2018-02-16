package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class PopUpShowMidiMessageFragment extends DialogFragment {

    Midi m;
    ArrayList<String> songMidiMessages, songMidiMessagesNice;
    ArrayAdapter<String> midiMessagesAdapter;
    ListView songMessages;
    Button sendAll, editMidi;

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
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        // safety check
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog());
        }
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
            PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.dismiss();
        }
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        View V = inflater.inflate(R.layout.popup_showmidimessages, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.midi));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe, getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setVisibility(View.GONE);

        // Initialise the Midi - this is only available for Marshmallow+
        m = new Midi();

        // Initialise the views
        songMessages = V.findViewById(R.id.songMessages);
        sendAll = V.findViewById(R.id.sendAll);
        editMidi = V.findViewById(R.id.editMidi);

        // Initialise the lists
        initialiseArrays();
        songMessages.setAdapter(midiMessagesAdapter);

        // Set listeners
        songMessages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                sendOneMessage(songMidiMessages.get(i));
            }
        });

        sendAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doSendAll();
            }
        });

        editMidi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMidiEdit();
            }
        });
        PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog());

        return V;
    }

    void doSendAll() {
        // Trigger the Override in StageMode/PresenterMode to send all the Midi messages stored
        if (mListener!=null) {
            mListener.sendMidi();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void sendOneMessage(String message) {
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
            FullscreenActivity.myToastMessage = getString(R.string.midi_error);
            ShowToast.showToast(getActivity());
        }
    }

    void openMidiEdit() {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            if (mListener != null) {
                FullscreenActivity.whichOptionMenu = "MIDI";
                mListener.prepareOptionMenu();
                mListener.openMyDrawers("option");
                FullscreenActivity.whattodo = "midicommands";
                mListener.openFragment();
            }
        } else {
            FullscreenActivity.myToastMessage = getString(R.string.nothighenoughapi);
            ShowToast.showToast(getActivity());
        }
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void initialiseArrays() {
        songMidiMessages = new ArrayList<>();
        songMidiMessagesNice = new ArrayList<>();
        if (m==null) {
            m = new Midi();
        }
        // Add what is there already
        String bits[] = FullscreenActivity.mMidi.trim().split("\n");
        try {
            for (String s : bits) {
                if (s!=null && !s.equals("") && !s.isEmpty()) {
                    // Get a human readable version of the midi code
                    String hr = m.getReadableStringFromHex(s,getActivity());
                    String message = hr + "\n" + "(" + s + ")";
                    songMidiMessagesNice.add(message);
                    songMidiMessages.add(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        midiMessagesAdapter = new ArrayAdapter<>(getActivity(),R.layout.my_spinner,songMidiMessagesNice);
    }
}
