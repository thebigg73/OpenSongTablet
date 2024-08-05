package com.garethevans.church.opensongtablet.chords;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.BottomSheetTransposeBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.setmenu.SetItemInfo;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class TransposeBottomSheet extends BottomSheetDialogFragment {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "TransposeBottomSheet";
    private boolean editSong = false;  // This is set to true when coming here from EditSongFragment
    private boolean transposeCapo, transposeSet, transposeVariation, assumePreferred, transposeCopy;
    private BottomSheetTransposeBinding myView;
    private MainActivityInterface mainActivityInterface;
    private int fromFormat, toFormat, prefFormat, transposeTimes, position;
    private String startKey, newKey, setFolder, songFolder;
    private String string_Key="", string_Transpose="", string_WebsiteChordsTranspose="",
        string_ChordFormatPreferredInfo="", string_DeeplinkChordSettings="", string_CopyOf="",
        string_Standard="", string_DetectedAppearance="", string_variation="**Variation",
            originalkey_string=""
    ;

    public TransposeBottomSheet(boolean editSong) {
        // This is called from the EditSongFragment.  Receive temp lyrics and key
        this.editSong = editSong;
    }

    public TransposeBottomSheet() {
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
        string_Transpose = getString(R.string.transpose);
        string_Key = getString(R.string.key);
        string_WebsiteChordsTranspose = getString(R.string.website_chords_transpose);
        string_ChordFormatPreferredInfo = getString(R.string.chordformat_preferred_info);
        string_DeeplinkChordSettings = getString(R.string.deeplink_chords_settings);
        string_CopyOf = getString(R.string.copy_of);
        string_Standard = getString(R.string.chordformat_1_name);
        string_DetectedAppearance = getString(R.string.chordformat_1);
        string_variation = "**"+context.getString(R.string.variation);
        originalkey_string = getString(R.string.key_original);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialog1 -> {
            try {
                BottomSheetDialog d = (BottomSheetDialog) dialog1;
                FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheet != null) {
                    BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                    BottomSheetBehavior.from(bottomSheet).setDraggable(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return dialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = BottomSheetTransposeBinding.inflate(inflater,container,false);
        myView.dialogHeading.setText(string_Transpose);
        myView.dialogHeading.setWebHelp(mainActivityInterface,string_WebsiteChordsTranspose);
        myView.dialogHeading.setClose(this);

        // Detect chord format - done on song load, but a recheck here incase the user changed the song
        mainActivityInterface.getTranspose().checkChordFormat(mainActivityInterface.getSong());

        // Set up views to match preferences
        setupViews();

        // Set the listeners
        setListeners();

        // Decide if we are using preferred chord format
        usePreferredChordFormat(mainActivityInterface.getPreferences().getMyPreferenceBoolean("chordFormatUsePreferred",false));

        return myView.getRoot();
    }

    private void setupViews() {
        // If this song is in the current set, show the transpose in set switch
        position = mainActivityInterface.getCurrentSet().getIndexSongInSet();
        songFolder = mainActivityInterface.getSong().getFolder();
        myView.transposeSetItem.setChecked(false);
        myView.transposeVariation.setChecked(false);
        myView.transposeCopy.setChecked(false);
        myView.transposeCapo.setChecked(false);

        myView.nestedScrollView.setFabToAnimate(myView.convertToFlats);
        myView.nestedScrollView.setFab2ToAnimate(myView.convertToSharps);
        myView.nestedScrollView.setExtendedFabToAnimate(myView.doTransposeButton);
        
        if (position>-1 && mainActivityInterface.getCurrentSet()!=null &&
                mainActivityInterface.getCurrentSet().getSetItemInfos()!=null &&
                position < mainActivityInterface.getCurrentSet().getCurrentSetSize()) {

            // In a set, so hide the song only transpose options
            myView.transposeCapo.setVisibility(View.GONE);
            myView.transposeCopy.setVisibility(View.GONE);

            // Show the set options
            SetItemInfo setItemInfo = mainActivityInterface.getCurrentSet().getSetItemInfo(position);

            if (setItemInfo.songfolder.contains("**Variation") || setItemInfo.songfolder.contains(string_variation)) {
                // Hide variation creation and the set item transpose
                myView.transposeVariation.setVisibility(View.GONE);
                myView.transposeSetItem.setVisibility(View.GONE);

            } else {
                // Allow variation creation and setItem transpose (default)
                myView.transposeVariation.setVisibility(View.VISIBLE);
                myView.transposeSetItem.setVisibility(View.VISIBLE);
                myView.transposeSetItem.setChecked(true);
            }

        } else {
            // Not in a set, or the set item doesn't have a key set, so hide the set options
            setFolder = songFolder;
            myView.transposeSetItem.setVisibility(View.GONE);
            myView.transposeVariation.setVisibility(View.GONE);

            // Show the song options
            myView.transposeCapo.setVisibility(View.VISIBLE);
            myView.transposeCopy.setVisibility(View.VISIBLE);
        }

        // If we have a key set and an original key set and they are different, show the transpose original
        checkTransposeOriginal();

        myView.transposeSlider.setValue(0);

        // Get the key of the song if set
        if (editSong && mainActivityInterface.getTempSong()!=null) {
            startKey = mainActivityInterface.getTempSong().getKey();
        } else {
            startKey = mainActivityInterface.getSong().getKey();
        }

        try {
            if (startKey.isEmpty()) {
                myView.keyChangeTextView.setText(getTransposeKey("0"));
            } else {
                myView.keyChangeTextView.setText(getTransposeKey(startKey));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set up the exposed dropdown for chord formats and set to detected
        buildChordFormatOptions();
        int detectedFormatNum = mainActivityInterface.getSong().getDetectedChordFormat();
        if (detectedFormatNum<1) {
            detectedFormatNum = 1;
        }
        String detectedName = string_Standard;
        if (mainActivityInterface.getTranspose().getChordFormatNames().size()>(detectedFormatNum-1)) {
            detectedName = mainActivityInterface.getTranspose().getChordFormatNames().get(detectedFormatNum-1);
        }

        String detectedAppearance = string_DetectedAppearance;
        if (mainActivityInterface.getTranspose().getChordFormatAppearances().size()>(detectedFormatNum-1)) {
            detectedAppearance = mainActivityInterface.getTranspose().getChordFormatAppearances().get(detectedFormatNum-1);
        }

        myView.chordFormatFrom.setText(detectedName);
        myView.chordFormatTo.setText(detectedName);
        String detectedSummary = detectedName + ": " + detectedAppearance;
        myView.warningFormatMatch.setHint(detectedSummary);

        myView.assumePreferred.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean(
                "chordFormatUsePreferred", false));
        prefFormat = mainActivityInterface.getPreferences().getMyPreferenceInt(
                "chordFormat",1);
        if (prefFormat<1) {
            prefFormat = 1;
        }
        String hint = string_ChordFormatPreferredInfo + " " + string_Standard + ": " + string_DetectedAppearance;
        if (mainActivityInterface.getTranspose().getChordFormatAppearances().size()>(prefFormat-1)) {
            hint = string_ChordFormatPreferredInfo + " " +
                    mainActivityInterface.getTranspose().getChordFormatNames().get(prefFormat-1) +
                    ": " + mainActivityInterface.getTranspose().getChordFormatAppearances().get(prefFormat-1);
        }
        myView.assumePreferred.setHint(hint);

        usePreferredChordFormat(myView.assumePreferred.getChecked());
    }

    private void buildChordFormatOptions() {
        if (getContext()!=null && !mainActivityInterface.getTranspose().getChordFormatNames().isEmpty()) {
            ExposedDropDownArrayAdapter exposedDropDownArrayAdapterFrom = new ExposedDropDownArrayAdapter(getContext(),
                    myView.chordFormatFrom, R.layout.view_exposed_dropdown_item,
                    mainActivityInterface.getTranspose().getChordFormatNames());
            ExposedDropDownArrayAdapter exposedDropDownArrayAdapterTo = new ExposedDropDownArrayAdapter(getContext(),
                    myView.chordFormatTo, R.layout.view_exposed_dropdown_item,
                    mainActivityInterface.getTranspose().getChordFormatNames());
            myView.chordFormatFrom.setAdapter(exposedDropDownArrayAdapterFrom);
            myView.chordFormatTo.setAdapter(exposedDropDownArrayAdapterTo);
        }
    }

    private String getTransposeKey(String newKey) {
        if (startKey==null || startKey.isEmpty() || startKey.equals("0")) {
            return newKey;
        } else {
            return string_Key + ": " + mainActivityInterface.getTranspose().getFixedKey(startKey) + "\n" +
                    string_Transpose + ": " + mainActivityInterface.getTranspose().getFixedKey(newKey);
        }
    }
    private void setListeners() {
        //0=-6, 1=-5, 2=-4, 3=-3, 4=-2, 5=-1, 6=0, 7=1, 8=2, 9=3, 10=4, 11=5, 12=6
        myView.transposeSlider.addOnChangeListener((slider, value, fromUser) -> {
            // Update the text
            String thisNewKey;
            if (startKey==null || startKey.isEmpty() || startKey.equals("0")) {
                if (value>0) {
                    thisNewKey = "+" + (int)value;
                } else {
                    thisNewKey = String.valueOf((int) value);
                }
                myView.keyChangeTextView.setText(thisNewKey);
                newKey = "";
            } else {
                // We need to get the transposed key
                String keyToNum = mainActivityInterface.getTranspose().keyToNumber(startKey);
                if (value<0) {
                    newKey = mainActivityInterface.getTranspose().transposeNumber(keyToNum,"-1",(int)Math.abs(value));
                } else if (value>0) {
                    newKey = mainActivityInterface.getTranspose().transposeNumber(keyToNum,"+1",(int)Math.abs(value));
                } else {
                    newKey = startKey;
                }
                newKey = mainActivityInterface.getTranspose().numberToKey(newKey);
                Log.d(TAG,"startKey:"+startKey+"  newKey:"+newKey);
                myView.keyChangeTextView.setText(getTransposeKey(newKey));
                checkTransposeOriginal();
            }
        });

        myView.assumePreferred.getSwitch().setOnCheckedChangeListener((buttonView, isChecked) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("chordFormatUsePreferred",isChecked);
            usePreferredChordFormat(isChecked);
        });

        myView.transposeSetItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                myView.transposeVariation.setChecked(false);
            }
        });
        myView.transposeVariation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                myView.transposeSetItem.setChecked(false);
            }
        });

        myView.doTransposeButton.setOnClickListener(v -> doTranspose());

        myView.convertToFlats.setOnClickListener(v -> {
            mainActivityInterface.getTranspose().setConvertToFlats(true);
            doTranspose();
        });

        myView.convertToSharps.setOnClickListener(v -> {
            mainActivityInterface.getTranspose().setConvertToSharps(true);
            doTranspose();
        });

        myView.chordsFormat.setOnClickListener(view -> {
            mainActivityInterface.navigateToFragment(string_DeeplinkChordSettings,0);
            dismiss();
        });

        myView.transposeCapo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                myView.capoChange.setVisibility(View.VISIBLE);
            } else {
                myView.capoChange.setVisibility(View.INVISIBLE);
            }
        });

        myView.transposeOriginal.setOnClickListener(view -> {
            // Get the keynumber of the original key and current key (both are set for this button to be active and visible)
            int transposeTimes = mainActivityInterface.getTranspose().getTransposeTimes(mainActivityInterface.getSong().getKey(),
                    mainActivityInterface.getSong().getKeyOriginal());
            myView.transposeSlider.setValue(transposeTimes);
        });
    }

    private void usePreferredChordFormat(boolean trueorfalse) {
        // Use preferred chord format or detected one
        int formattouse;

        if (trueorfalse) {
            formattouse = prefFormat;
            myView.chordFormat.setVisibility(View.GONE);
            if (prefFormat==mainActivityInterface.getSong().getDetectedChordFormat()) {
                myView.warningFormatMatch.setVisibility(View.GONE);
            } else {
                myView.warningFormatMatch.setTextColor(Color.RED);
                myView.warningFormatMatch.setHintColor(Color.RED);
                myView.warningFormatMatch.setVisibility(View.VISIBLE);
            }

        } else {
            formattouse = mainActivityInterface.getSong().getDetectedChordFormat();
            myView.chordFormat.setVisibility(View.VISIBLE);
            myView.warningFormatMatch.setVisibility(View.GONE);
        }

        if (formattouse<1) {
            formattouse = 1;
        }
        if (mainActivityInterface.getTranspose().getChordFormatNames().size()>(formattouse-1)) {
            myView.chordFormatFrom.setText(mainActivityInterface.getTranspose().getChordFormatNames().get(formattouse - 1));
            myView.chordFormatTo.setText(mainActivityInterface.getTranspose().getChordFormatNames().get(formattouse - 1));
        }
    }

    private void checkTransposeOriginal() {
        boolean hasKey = mainActivityInterface.getSong().getKey()!=null &&
                !mainActivityInterface.getSong().getKey().isEmpty();
        boolean hasOriginalKey = mainActivityInterface.getSong().getKeyOriginal()!=null &&
                !mainActivityInterface.getSong().getKeyOriginal().isEmpty();
        boolean showTransposeOriginal = hasKey && hasOriginalKey &&
                !mainActivityInterface.getSong().getKey().equals(mainActivityInterface.getSong().getKeyOriginal());
        if (hasOriginalKey) {
            myView.transposeOriginal.setText(originalkey_string+" ("+mainActivityInterface.getSong().getKeyOriginal()+")");
        } else {
            myView.transposeOriginal.setText(originalkey_string);
        }
        myView.transposeOriginal.setVisibility(showTransposeOriginal? View.VISIBLE:View.GONE);
        myView.transposeOriginalDivider.setVisibility(showTransposeOriginal? View.VISIBLE:View.GONE);
    }

    private void getValues() {
        if (assumePreferred) {
            fromFormat = mainActivityInterface.getPreferences().getMyPreferenceInt(
                    "chordFormat", 1);
            toFormat = fromFormat;
        } else {
            // Overriding the preferred defaults
            if (mainActivityInterface.getTranspose().getChordFormatNames().contains(myView.chordFormatFrom.getText().toString())) {
                fromFormat = mainActivityInterface.getTranspose().getChordFormatNames().indexOf(myView.chordFormatFrom.getText().toString()) + 1;
                toFormat = mainActivityInterface.getTranspose().getChordFormatNames().indexOf(myView.chordFormatTo.getText().toString()) + 1;
            } else {
                fromFormat = 1;
                toFormat = 1;
            }
        }
        // Update the song detected chord format as required by manual change
        mainActivityInterface.getSong().setDetectedChordFormat(fromFormat);
        mainActivityInterface.getSong().setDesiredChordFormat(toFormat);
    }

    private void doTranspose() {
        getValues();
        transposeSet = myView.transposeSetItem.getChecked();
        transposeVariation = myView.transposeVariation.getChecked();
        transposeCapo = myView.transposeCapo.getChecked();
        transposeCopy = myView.transposeCopy.getChecked();
        transposeTimes = (int) myView.transposeSlider.getValue();
        assumePreferred = myView.assumePreferred.getChecked();

        // Need to decide what file gets transposed
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // Set the force reload flag
            mainActivityInterface.getTranspose().setForceReload(true);

            String transposeDirection;

            // Simplify slider to minimum number of transpose steps
            // Why transpose up 11 times, when you can just transpose down once.
            // Giving the option as it makes it easier for the user to select new key
            if (transposeTimes > 6) {
                // 7>-5  8>-4 9>-3 10>-2 11>-1 12>0
                transposeTimes = transposeTimes - 12;
            } else if (transposeTimes < -6) {
                // -7>5 -8>4 -9>3 -10>2 -11>1 -12>0
                transposeTimes = 12 + transposeTimes;
            }

            Log.d(TAG,"transposeTimes before simplify:"+transposeTimes);
            if (transposeTimes >= 0) {
                transposeDirection = "+1";
            } else {
                transposeDirection = "-1";
            }

            transposeTimes = Math.abs(transposeTimes);

            Log.d(TAG,"transposeTimes after simplify:"+transposeTimes + " direction:"+transposeDirection);

            Log.d(TAG,"songFolder:"+songFolder+"   setFolder:"+setFolder);
            if (songFolder==null) {
                songFolder = mainActivityInterface.getMainfoldername();
            }
            if (setFolder==null) {
                setFolder = songFolder;
            }
            // If we are in a set (position>-1)
            if (position>-1 && mainActivityInterface.getCurrentSet().getSetItemInfos()!=null && mainActivityInterface.getCurrentSet().getCurrentSetSize()>position) {
                // Transpose the key in the set.
                // This deals with normal songs and songs that are already had temp key changes from the set list
                try {
                    mainActivityInterface.getCurrentSet().getSetItemInfo(position).songkey = newKey;
                    // Notify the set adapter (and the inline set)
                    mainActivityInterface.notifySetFragment("changed",position);

                    if ((songFolder.equals("**Variation") || songFolder.equals(string_variation)) && (!setFolder.contains("**Variation") && !setFolder.contains(string_variation))) {
                        // This song is already a temp variation that is transposed
                        // We need to call the original file
                        mainActivityInterface.getSong().setFolder(setFolder);
                        mainActivityInterface.getLoadSong().doLoadSong(mainActivityInterface.getSong(), false);
                    }
                    if ((setFolder!=null && !setFolder.contains("**Variation") && !setFolder.contains(string_variation)) && !transposeSet && !transposeVariation) {
                        // If this is a normal song and want to actually transpose it normally, transpose and resave
                        mainActivityInterface.getSong().setFolder(setFolder);
                        mainActivityInterface.getTranspose().doTranspose(mainActivityInterface.getSong(),
                                transposeDirection, transposeTimes, fromFormat, toFormat);
                        mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(),true);

                    } else if ((setFolder!=null && !setFolder.contains("**Variation") && !setFolder.contains(string_variation)) && transposeVariation) {
                        // If this is a normal song, but want to convert to a variation
                        mainActivityInterface.getVariations().makeVariation(position);

                    } else if ((setFolder!=null && setFolder.contains("Variation")) || (setFolder!=null && setFolder.contains(string_variation.replace("**","")))) {
                        // This song was already a variation (no option to transposeSet or transposeVariation)
                        mainActivityInterface.getTranspose().doTranspose(mainActivityInterface.getSong(),
                                transposeDirection, transposeTimes, fromFormat, toFormat);
                        mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(),true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mainActivityInterface.getStorageAccess().updateFileActivityLog(e.toString());
                }

                // If this is a normal song, but we want to just transpose in the set list,
                // Nothing more is required

                // Update the set list/menu
                mainActivityInterface.getMainHandler().post(() -> {
                    try {
                        mainActivityInterface.getSetActions().saveTheSet();
                        mainActivityInterface.updateSetList();
                        if (position > -1) {
                            mainActivityInterface.loadSongFromSet(position);
                        }
                        dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                        mainActivityInterface.getStorageAccess().updateFileActivityLog(e.toString());
                    }
                });

            } else {
                // Not in a set
                // If requested transpose the capo fret number
                try {
                    String newFilename = mainActivityInterface.getSong().getFilename() + " (" + string_CopyOf + ")";
                    String newTitle = mainActivityInterface.getSong().getTitle() + " (" + string_CopyOf + ")";
                    String newCapo = String.valueOf(((Integer.parseInt("0" + mainActivityInterface.getSong().getCapo()) + 12 +
                            (transposeTimes * Integer.parseInt(transposeDirection))) % 12));
                    if (newCapo.equals("0")) {
                        newCapo = "";
                    }

                    // All options require transpose
                    mainActivityInterface.getTranspose().doTranspose(mainActivityInterface.getSong(),
                            transposeDirection, transposeTimes, fromFormat, toFormat);

                    if (transposeCapo) {
                        mainActivityInterface.getSong().setCapo(newCapo);
                    }

                    if (transposeCopy) {
                        // Make a copy of the song that is transposed (leaving the original untouched)
                        mainActivityInterface.getSong().setFilename(newFilename);
                        mainActivityInterface.getSong().setTitle(newTitle);
                        mainActivityInterface.getSQLiteHelper().createSong(songFolder, newFilename);
                        mainActivityInterface.getSaveSong().doSave(mainActivityInterface.getSong());

                    } else {
                        // Just update the song and be done with it!
                        mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(),true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mainActivityInterface.getStorageAccess().updateFileActivityLog(e.toString());
                }

                // Update the song menu and load the song again
                mainActivityInterface.getMainHandler().post(() -> {
                    try {
                        mainActivityInterface.updateSongMenu(mainActivityInterface.getSong());
                        mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(),true);
                        mainActivityInterface.doSongLoad(mainActivityInterface.getSong().getFolder(),
                                mainActivityInterface.getSong().getFilename(), true);
                        dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                        mainActivityInterface.getStorageAccess().updateFileActivityLog(e.toString());
                    }
                });
            }
        });
    }


}
