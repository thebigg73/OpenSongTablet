package com.garethevans.church.opensongtablet.drummer;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.BottomSheetCommon;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.BottomSheetDrummerFileBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

public class DrummerFileBottomSheet extends BottomSheetCommon {

    private final String TAG = "DrummerFileBS";
    private DrumSequencerFragment drumSequencerFragment;
    private MainActivityInterface mainActivityInterface;
    private String drummer_string="";
    private String website_string="";
    private String load_string="";
    private String save_string="";
    private String assign_string="";
    private String assign_info_string="";
    private String need_to_save_string="";
    private String success_string="";
    private BottomSheetDrummerFileBinding myView;
    private String whichView, filename, timeSig, drummerKit;


    public DrummerFileBottomSheet(DrumSequencerFragment drumSequencerFragment, String whichView,
                                  String filename, String timeSig, String drummerKit) {
        // This is called from the EditSongFragment.  Receive temp lyrics and key
        this.drumSequencerFragment = drumSequencerFragment;
        this.whichView = whichView;
        this.filename = filename;
        this.timeSig = timeSig;
        this.drummerKit = drummerKit;
    }

    public DrummerFileBottomSheet() {
        // Null initialised for when we come here from performance/presentation/stage mode
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        prepareStrings();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = BottomSheetDrummerFileBinding.inflate(inflater,container,false);

        prepareStrings();

        myView.dialogHeading.setText(drummer_string);
        myView.dialogHeading.setWebHelp(mainActivityInterface,website_string);
        myView.dialogHeading.setClose(this);

        // Set up views and listeners to match preferences
        setupViews();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            drummer_string = getString(R.string.drummer);
            website_string = getString(R.string.website_drummer);
            load_string = getString(R.string.load);
            save_string = getString(R.string.save);
            assign_string = getString(R.string.drummer_assign);
            assign_info_string = getString(R.string.drummer_assign_info) + ": " + mainActivityInterface.getSong().getFilename();
            need_to_save_string = getString(R.string.drummer_assign_error);
            success_string = getString(R.string.success);
        }
    }

    private void setupViews() {
        if (getContext()!=null) {
            myView.loadFile.setVisibility(whichView.equals("load") ? View.VISIBLE : View.GONE);
            myView.saveFile.setVisibility(whichView.equals("save") ? View.VISIBLE : View.GONE);
            myView.assignFile.setText(assign_info_string);
            myView.assignFile.setVisibility(whichView.equals("assign") ? View.VISIBLE : View.GONE);
            switch (whichView) {
                case "load":
                    ExposedDropDownArrayAdapter loadFileAdapter = new ExposedDropDownArrayAdapter(getContext(),
                            myView.loadFile, R.layout.view_exposed_dropdown_item,
                            mainActivityInterface.getDrumViewModel().getDrummer().getDrummerFiles(null, true));
                    myView.loadFile.setAdapter(loadFileAdapter);
                    myView.actionButton.setOnClickListener(view -> loadFile());
                    myView.actionButton.setText(load_string);
                    break;

                case "save":
                    myView.saveFile.setText(filename);
                    myView.actionButton.setOnClickListener(view -> saveFile());
                    myView.actionButton.setText(save_string);
                    break;

                case "assign":
                    ArrayList<String> drummerFiles = mainActivityInterface.getDrumViewModel().getDrummer().getDrummerFiles(timeSig, true);
                    myView.actionButton.setText(assign_string);
                    String filenameToLookFor = mainActivityInterface.getDrumViewModel().getDrummer().getFilenameFromBasics(filename, timeSig);
                    String niceNameToLookFor = mainActivityInterface.getDrumViewModel().getDrummer().getNiceNameFromBasics(filename, timeSig);

                    Log.d(TAG, "filenameToLookFor:" + filenameToLookFor + "  niceNameToLookFor:" + niceNameToLookFor);
                    Log.d(TAG, "timeSig:" + timeSig + "  drummerFiles.contains(" + niceNameToLookFor + "):" + drummerFiles.contains(niceNameToLookFor));
                    if (drummerFiles.contains(niceNameToLookFor)) {
                        filename = filenameToLookFor;
                        myView.actionButton.setOnClickListener(view -> assignFile());
                    } else {
                        // Alert the user that they need to save the drummer file before assigning it
                        myView.actionButton.setEnabled(false);
                        mainActivityInterface.getShowToast().doItBottomSheet(need_to_save_string, myView.getRoot());
                        dismiss();
                    }
                    break;
            }
        }
    }

    private void loadFile() {
        if (myView==null || myView.loadFile.getText()==null || myView.loadFile.getText().toString().isEmpty()) {
            dismiss();
        } else {
            String filenameToLoad = mainActivityInterface.getDrumViewModel().getDrummer().
                    getFilenameFromNiceName(myView.loadFile.getText().toString());
            Log.d(TAG,"filenameToLoad:"+filenameToLoad);
            mainActivityInterface.getDrumViewModel().getDrummer().loadDrummerFile(filenameToLoad);
            mainActivityInterface.getDrumViewModel().getDrummer().updateActiveMap();
            mainActivityInterface.getDrumViewModel().updateDrummerAndTimer();
            drumSequencerFragment.updateViews();
            dismiss();
        }
    }

    private void saveFile() {
        if (myView == null || myView.saveFile.getText() == null || myView.saveFile.getText().toString().isEmpty()) {
            dismiss();
        } else {
            int[] timeSignature = DrumCalculations.getFixedTimeSignature(timeSig);
            String name = myView.saveFile.getText().toString().trim();
            filename = mainActivityInterface.getDrumViewModel().getDrummer().getFilenameFromBasics(name,timeSig);
            mainActivityInterface.getDrumViewModel().setDrumPatternJson(mainActivityInterface.getDrumViewModel().getCurrentPattern().getValue());
            mainActivityInterface.getDrumViewModel().getDrumPatternJson().setName(name);
            mainActivityInterface.getDrumViewModel().getDrumPatternJson().setBeats(timeSignature[0]);
            mainActivityInterface.getDrumViewModel().getDrumPatternJson().setDivisions(timeSignature[1]);
            mainActivityInterface.getDrumViewModel().getDrummer().saveDrummerFile(filename);
            drumSequencerFragment.updateFilename(name);
            dismiss();
        }
    }

    private void assignFile() {
        // We can only get here if the filename already exists and we have set the timeSig
        // We need to make sure the song now has the same time signature
        String timeSig = mainActivityInterface.getDrumViewModel().getThisBeats() + "/" +
                mainActivityInterface.getDrumViewModel().getThisDivisions();
        mainActivityInterface.getSong().setTimesig(timeSig);
        mainActivityInterface.getSong().setDrummer(filename);
        mainActivityInterface.getSong().setDrummerKit(drummerKit);
        mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(),false);
        mainActivityInterface.getShowToast().doItBottomSheet(success_string,myView.getRoot());
        dismiss();
    }
}

