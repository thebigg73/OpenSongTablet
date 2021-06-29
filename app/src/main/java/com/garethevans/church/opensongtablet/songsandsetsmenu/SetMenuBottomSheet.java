package com.garethevans.church.opensongtablet.songsandsetsmenu;

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
import com.garethevans.church.opensongtablet.databinding.MenuSetsDialogBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SetMenuBottomSheet extends BottomSheetDialogFragment {

    MenuSetsDialogBinding myView;
    MainActivityInterface mainActivityInterface;

    String fragName;
    Fragment callingFragment;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        mainActivityInterface.songMenuActionButtonShow(true);
        dismiss();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = MenuSetsDialogBinding.inflate(inflater, container, false);

        myView.createSet.setOnClickListener(v -> mainActivityInterface.displayAreYouSure("newSet",getString(R.string.set_new),null,fragName,callingFragment,null));
        //menuSetsDialogBinding.saveSet.setOnClickListener(v -> setActions.saveTheSet());
        //menuSetsDialogBinding.manageSets.setOnClickListener(v -> mainActivityInterface.navigateToFragment(R.id.nav_manageSets));
        //menuSetsDialogBinding.addCustomSlide.setOnClickListener(v -> mainActivityInterface.navigateToFragment(R.id.customSlide));

        return myView.getRoot();
    }

}
