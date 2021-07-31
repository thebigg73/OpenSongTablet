package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.databinding.EditSongLyricsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

// This fragment purely deals with the lyrics/chords

public class EditSongFragmentLyrics extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private EditSongLyricsBinding myView;
    private final String TAG = "EditSongFragmentLyrics";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = EditSongLyricsBinding.inflate(inflater, container, false);

        // Put the values in
        setupValues();

        // Add listeners
        setupListeners();

        return myView.getRoot();
    }

    private void setupValues() {
        myView.lyrics.setText(mainActivityInterface.getTempSong().getLyrics());
        myView.lyrics.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        myView.lyrics.setImeOptions(EditorInfo.IME_ACTION_NONE);
        myView.lyrics.setHorizontallyScrolling(true);
        myView.lyrics.setAutoSizeTextTypeUniformWithConfiguration(8,18,1);
        checkLines();
    }

    private void setupListeners() {
        myView.lyrics.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkLines();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mainActivityInterface.getTempSong().setLyrics(editable.toString());
                mainActivityInterface.showSaveAllowed(mainActivityInterface.songChanged());
            }
        });
    }

    private void checkLines() {
        String[] lines = myView.lyrics.getText().toString().split("\n");
        int num = lines.length;
        Log.d(TAG,"lines="+num);
        if (num > 20) {
            myView.lyrics.setLines(lines.length);
            myView.lyrics.setMinLines(lines.length);
            myView.lyrics.setLines(lines.length);
        } else {
            myView.lyrics.setLines(20);
            myView.lyrics.setMinLines(20);
            myView.lyrics.setLines(20);
        }

        Log.d(TAG,"lines="+myView.lyrics.getLines());
        Log.d(TAG,"minLines="+myView.lyrics.getMinLines());
        Log.d(TAG,"maxLines="+myView.lyrics.getMaxLines());
        Log.d(TAG,"inputType="+myView.lyrics.getInputType()+"  (want "+ InputType.TYPE_TEXT_FLAG_MULTI_LINE+")");
        Log.d(TAG,"imeOptions="+myView.lyrics.getImeOptions()+"  (want "+ EditorInfo.IME_ACTION_NONE+")");

    }

    public void changelyricFormat() {
        if (mainActivityInterface.getPreferences().getMyPreferenceBoolean(requireContext(),"editAsChordPro",false)) {
            myView.lyrics.setText(mainActivityInterface.getConvertChoPro().
                    fromOpenSongToChordPro(requireContext(), mainActivityInterface, mainActivityInterface.getTempSong().getLyrics()));
        } else {
            myView.lyrics.setText(mainActivityInterface.getConvertChoPro().
                    fromChordProToOpenSong(mainActivityInterface.getTempSong().getLyrics()));
        }
    }


    // The stuff for the bottom sheet

    /*



        activeColor = requireContext().getResources().getColor(R.color.colorSecondary);
        inactiveColor = requireContext().getResources().getColor(R.color.colorPrimary);


    if (editAsChoPro) {
            // Do the conversion
            // Initially set this to false so it triggers
            editAsChoPro = false;
            dealWithEditMode(true);
            setButtonOn(chordProFormat,true);
            setButtonOn(openSongFormat,false);
        } else {
            setButtonOn(chordProFormat,false);
            setButtonOn(openSongFormat,true);
        }


    private void setButtonOn(MaterialButton button, boolean on) {
        if (on) {
            button.setBackgroundTintList(ColorStateList.valueOf(activeColor));
        } else {
            button.setBackgroundTintList(ColorStateList.valueOf(inactiveColor));
        }
    }
     */

}
