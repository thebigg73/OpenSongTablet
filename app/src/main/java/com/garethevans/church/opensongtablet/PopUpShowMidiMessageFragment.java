/*
package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.garethevans.church.opensongtablet.OLD_TO_DELETE._CustomAnimations;
import com.garethevans.church.opensongtablet.OLD_TO_DELETE._PopUpSizeAndAlpha;
import com.garethevans.church.opensongtablet.OLD_TO_DELETE._ShowToast;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
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
import java.util.Objects;

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
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        // safety check
        super.onAttach(activity);
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
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        View V = inflater.inflate(R.layout.popup_showmidimessages, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getResources().getString(R.string.midi));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _CustomAnimations.animateFAB(closeMe, getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        // Initialise the Midi - this is only available for Marshmallow+
        m = new Midi();

        _Preferences preferences = new _Preferences();

        // Initialise the views
        ListView songMessages = V.findViewById(R.id.songMessages);
        Button sendAll = V.findViewById(R.id.sendAll);
        Button editMidi = V.findViewById(R.id.editMidi);

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
        _PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog(), preferences);

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
            _ShowToast.showToast(getActivity());
        }
    }

    private void openMidiEdit() {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            if (mListener != null) {
                StaticVariables.whichOptionMenu = "MIDI";
                mListener.prepareOptionMenu();
                mListener.openMyDrawers("option");
                StaticVariables.whattodo = "midicommands";
                mListener.openFragment();
            }
        } else {
            StaticVariables.myToastMessage = getString(R.string.nothighenoughapi);
            _ShowToast.showToast(getActivity());
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
                if (s!=null && !s.equals("") && !s.isEmpty() && getActivity()!=null) {
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
        midiMessagesAdapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()),R.layout.my_spinner, songMidiMessagesNice);
    }
}
*/
