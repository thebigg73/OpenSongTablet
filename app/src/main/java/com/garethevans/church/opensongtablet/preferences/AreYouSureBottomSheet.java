package com.garethevans.church.opensongtablet.preferences;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.BottomSheetCommon;
import com.garethevans.church.opensongtablet.databinding.BottomSheetAreYouSureBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.ArrayList;

public class AreYouSureBottomSheet extends BottomSheetCommon {

    private MainActivityInterface mainActivityInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    private final String textToShow, what, fragName;
    private final ArrayList<String> arguments;
    private final Fragment callingFragment;  // can be null if not needed for MainActivity to refresh the fragment
    private final Song song;

    public AreYouSureBottomSheet(String what, String textToShow, ArrayList<String> arguments, String fragName, Fragment callingFragment, Song song) {
        this.what = what;               // Variable passed to MainActivity to trigger required action
        this.textToShow = textToShow;   // Information displayed about what is about to happen
        this.arguments = arguments;     // Extra info passed back.  Can be null
        this.fragName = fragName;       // The fragment requesting confirmation
        this.callingFragment = callingFragment;
        this.song = song;
    }

    public AreYouSureBottomSheet() {
        // Default constructor required to avoid re-instantiation failures
        // Just close the bottom sheet
        what = "";
        textToShow = "";
        arguments = new ArrayList<>();
        fragName = "";
        callingFragment = null;
        song = new Song();
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        BottomSheetAreYouSureBinding myView = BottomSheetAreYouSureBinding.inflate(inflater, container, false);

        // Initialise the 'close' floatingactionbutton
        myView.dialogHeading.setClose(this);

        // If we are auto starting Nearby, show that prompt to make sense
        if ((what.equals("NearbyAdvertise") || what.equals("NearbyDiscover")) &&
                mainActivityInterface.getNearbyActions().getNearbyConnectionManagement().getNearbyStartOnBoot()) {
            myView.dialogHeading.setText(getString(R.string.connections_start_on_boot));
        }

        if (what.equals("newSet") && getContext()!=null) {
            myView.dialogHeading.setWebHelp(mainActivityInterface,getString(R.string.website_set_create_new));
        }

        String text = textToShow;

        if ((what.equals("openChordsForcePush") || what.equals("openChordsForcePull")) &&
            arguments!=null && !arguments.isEmpty()) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(text);
                stringBuilder.append("\n\n");
                for (String arg : arguments) {
                    stringBuilder.append(arg).append("\n");
                }
                text = stringBuilder.toString();
        }

        myView.action.setText(text);
        myView.okButton.setOnClickListener(v -> {
            dismiss();
            mainActivityInterface.confirmedAction(true,what,arguments,fragName,callingFragment,song);
        });
        return myView.getRoot();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (what.equals("exit")) {
            mainActivityInterface.setAlreadyBackPressed(false);
        }
    }
}
