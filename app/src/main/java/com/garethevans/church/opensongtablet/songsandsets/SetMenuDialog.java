package com.garethevans.church.opensongtablet.songsandsets;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.MenuSetsDialogBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;

public class SetMenuDialog extends DialogFragment {

    MenuSetsDialogBinding menuSetsDialogBinding;
    MainActivityInterface mainActivityInterface;

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
        menuSetsDialogBinding = MenuSetsDialogBinding.inflate(inflater, container, false);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        SongForSet songForSet = new SongForSet();
        Preferences preferences = new Preferences();
        SetActions setActions = new SetActions();

        menuSetsDialogBinding.createSet.setOnClickListener(v -> mainActivityInterface.displayAreYouSure("newSet",getActivity().getResources().getString(R.string.options_clearthisset),null));
        //menuSetsDialogBinding.saveSet.setOnClickListener(v -> setActions.saveTheSet());
        //menuSetsDialogBinding.manageSets.setOnClickListener(v -> mainActivityInterface.navigateToFragment(R.id.nav_manageSets));
        //menuSetsDialogBinding.addCustomSlide.setOnClickListener(v -> mainActivityInterface.navigateToFragment(R.id.customSlide));

        return menuSetsDialogBinding.getRoot();
    }


}
