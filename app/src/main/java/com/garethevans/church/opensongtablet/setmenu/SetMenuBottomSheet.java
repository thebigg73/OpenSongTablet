package com.garethevans.church.opensongtablet.setmenu;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.BottomSheetMenuSetBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songmenu.RandomSongBottomSheet;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SetMenuBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetMenuSetBinding myView;
    private MainActivityInterface mainActivityInterface;

    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private static final String TAG = "SetMenuBottomSheet";
    private String website_menu_set_string="", set_new_string="", deeplink_sets_string="",
            deeplink_bible_string="", deeplink_custom_slide_string="";

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
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetMenuSetBinding.inflate(inflater, container, false);

        prepareStrings();

        // Initialise the 'close' floatingactionbutton
        myView.dialogHeading.setClose(this);
        myView.dialogHeading.setWebHelp(mainActivityInterface,website_menu_set_string);

        // Check views allowed
        checkViewsAllowed();

        // Set up listeners
        setListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            website_menu_set_string = getString(R.string.website_menu_set);
            set_new_string = getString(R.string.set_new);
            deeplink_sets_string = getString(R.string.deeplink_sets);
            deeplink_bible_string = getString(R.string.deeplink_bible);
            deeplink_custom_slide_string = getString(R.string.deeplink_custom_slide);
        }
    }
    private void checkViewsAllowed() {
        // Check there are songs!
        if (mainActivityInterface.getCurrentSet().getSetItemInfos()==null || mainActivityInterface.getCurrentSet().getCurrentSetSize()==0) {
            myView.edit.setVisibility(View.GONE);
            myView.shuffleSet.setVisibility(View.GONE);
            myView.randomSong.setVisibility(View.GONE);
        }
    }

    private void setListeners() {
        myView.createSet.setOnClickListener(v -> {
            mainActivityInterface.displayAreYouSure("newSet",set_new_string,null,null,null,null);
            dismiss();
        });
        myView.manageSet.setOnClickListener(v -> {
            mainActivityInterface.navigateToFragment(deeplink_sets_string,-1);
            dismiss();
        });
        myView.edit.setOnClickListener(v -> {
            if (getActivity()!=null) {
                SetEditItemBottomSheet setVariationBottomSheet = new SetEditItemBottomSheet();
                setVariationBottomSheet.show(getActivity().getSupportFragmentManager(), "setVariation");
                dismiss();
            }
        });
        myView.addScripture.setOnClickListener(v -> {
            mainActivityInterface.navigateToFragment(deeplink_bible_string,0);
            dismiss();
        });
        myView.addCustom.setOnClickListener(v -> {
            mainActivityInterface.navigateToFragment(deeplink_custom_slide_string,0);
            dismiss();
        });
        myView.randomSong.setOnClickListener(v -> {
            if (getActivity()!= null) {
                RandomSongBottomSheet randomSongBottomSheet = new RandomSongBottomSheet("set");
                randomSongBottomSheet.show(getActivity().getSupportFragmentManager(), "RandomBottomSheet");
                dismiss();
            }
        });
        myView.shuffleSet.setOnClickListener(v -> {
            mainActivityInterface.updateFragment("shuffleSet",null,null);
            dismiss();
        });
        myView.sortSet.setOnClickListener(v -> {
            mainActivityInterface.updateFragment("sortSet",null,null);
            dismiss();
        });
    }
}
