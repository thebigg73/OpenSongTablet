package com.garethevans.church.opensongtablet.preferences;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.appdata.InformationBottomSheet;
import com.garethevans.church.opensongtablet.databinding.BottomSheetProfileBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

public class ProfileBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetProfileBinding myView;
    private MainActivityInterface mainActivityInterface;
    private String dialog_title_string="";
    private String success_string="";
    private String error_string="";
    private String restart_string="";
    private String restart_required_string="";
    private final String whattodo = "saveprofile", TAG = "ProfileBottomSheet";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialog1 -> {
            FrameLayout bottomSheet = ((BottomSheetDialog) dialog1).findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetBehavior.from(bottomSheet).setDraggable(false);
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetProfileBinding.inflate(inflater, container, false);

        prepareStrings();

        // Initialise the 'close' floatingactionbutton
        myView.dialogHeading.setClose(this);
        myView.dialogHeading.setText(dialog_title_string);

        // Set up the views
        setupViews();
        setListeners();

        return myView.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        prepareStrings();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            dialog_title_string = getString(R.string.profile);
            String load_string = getString(R.string.load);
            String save_string = getString(R.string.save);
            success_string = getString(R.string.success);
            error_string = getString(R.string.error);
            if (mainActivityInterface.getWhattodo().equals(whattodo)) {
                dialog_title_string = dialog_title_string + ": " + save_string;
            } else {
                dialog_title_string = dialog_title_string + ": " + load_string;
            }
            restart_string = getString(R.string.restart);
            restart_required_string = getString(R.string.restart_required);
        }
    }

    private void setupViews() {
        // Hide what we don't need
        if (mainActivityInterface.getWhattodo().equals(whattodo)) {
            myView.profileFileNameSave.setVisibility(View.VISIBLE);
            myView.saveButton.setVisibility(View.VISIBLE);
        } else {
            myView.profileFileNameSave.setVisibility(View.GONE);
            myView.saveButton.setVisibility(View.GONE);
        }
        // Get a list of the contents of the folder
        myView.existingFiles.removeAllViews();
        if (getContext()!=null) {
            int padding = (int)(mainActivityInterface.getDisplayDensity()*24);
            ArrayList<String> profiles = mainActivityInterface.getStorageAccess().listFilesInFolder("Profiles", "");
            for (String profile : profiles) {
                profile = trimOutXML(profile);
                MaterialTextView materialTextView = new MaterialTextView(getContext());
                materialTextView.setPadding(0,padding,0,padding);
                materialTextView.setText(profile);
                String finalProfile = profile;
                materialTextView.setOnClickListener(view -> updateName(finalProfile));
                myView.existingFiles.addView(materialTextView);
            }
        }
    }

    private void updateName(String profile) {
        if (myView!=null) {
            if (mainActivityInterface.getWhattodo().equals(whattodo)) {
                // Update the name for saving
                profile = trimOutXML(profile);
                myView.profileFileNameSave.setText(profile);
            } else {
                // Load this profile
                Uri loadUri = mainActivityInterface.getStorageAccess().getUriForItem("Profiles","",profile);
                boolean success = mainActivityInterface.getProfileActions().loadProfile(loadUri);
                mainActivityInterface.getPageButtons().setPreferences();
                mainActivityInterface.initialisePageButtons();
                mainActivityInterface.updatePageButtonLayout();
                mainActivityInterface.getShowToast().doIt(success ? success_string:error_string);
                InformationBottomSheet informationBottomSheet = new InformationBottomSheet(restart_string,
                        restart_required_string, restart_string, "restart");
                informationBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "restart");
                dismiss();
            }
        }
    }

    private void setListeners() {
        if (myView!=null) {
            myView.saveButton.setOnClickListener(view -> {
                String profileName = null;
                if (myView!=null && myView.profileFileNameSave.getText()!=null) {
                    profileName = myView.profileFileNameSave.getText().toString();
                }
                Log.d(TAG,"profileName:"+profileName);
                if (profileName!=null && !profileName.isEmpty()) {
                    profileName = trimOutXML(profileName);
                    profileName = addBackInXML(profileName);
                    Uri newUri = mainActivityInterface.getStorageAccess().getUriForItem("Profiles", "", profileName);
                    String result = mainActivityInterface.getProfileActions().saveProfile(newUri,profileName) ? success_string:error_string;
                    mainActivityInterface.getShowToast().doIt(result);
                    dismiss();
                }
            });
        }
    }

    private String trimOutXML(String profile) {
        // Only do this if we are saving
        if (mainActivityInterface.getWhattodo().equals(whattodo)) {
            profile = profile.replace(".xml","").replace(".XML","");
        }
        return profile;
    }

    private String addBackInXML(String profile) {
        // Only do this if we are saving
        if (mainActivityInterface.getWhattodo().equals(whattodo)) {
            profile = profile + ".xml";
        }
        return profile;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        try {
            mainActivityInterface.setWhattodo("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
