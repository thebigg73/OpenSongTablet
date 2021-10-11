package com.garethevans.church.opensongtablet.setprocessing;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsSetsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.TextInputBottomSheet;

import java.util.ArrayList;

public class SetActionsFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SettingsSetsBinding myView = SettingsSetsBinding.inflate(inflater, container, false);

        mainActivityInterface.updateToolbar(getString(R.string.set_manage));

        myView.createSet.setOnClickListener(v -> mainActivityInterface.displayAreYouSure("newSet",getString(R.string.set_new),null,"SetActionsFragment",this,null));
        myView.loadSet.setOnClickListener(v -> {
            mainActivityInterface.setWhattodo("loadset");
            mainActivityInterface.navigateToFragment(null,R.id.setManageFragment);
        });
        myView.saveSet.setOnClickListener(v -> {
            mainActivityInterface.setWhattodo("saveset");
            mainActivityInterface.navigateToFragment(null,R.id.setManageFragment);
        });
        myView.bibleButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.bible_graph));
        myView.slideButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.customSlideFragment));
        myView.exportSet.setOnClickListener(v -> exportSet());
        myView.backupSets.setOnClickListener(v -> {
            mainActivityInterface.setWhattodo("backupsets");
            mainActivityInterface.navigateToFragment(null,R.id.backupRestoreSetsFragment);
        });
        myView.restoreSets.setOnClickListener(v -> {
            mainActivityInterface.setWhattodo("restoresets");
            mainActivityInterface.navigateToFragment(null,R.id.backupRestoreSetsFragment);
        });

        return myView.getRoot();
    }

    private void exportSet() {
        // First up, get an arraylist of sets in the sets folder
        ArrayList<String> sets = mainActivityInterface.getStorageAccess().listFilesInFolder(requireContext(),
                mainActivityInterface,"Sets","");
        // Initiate the bottom sheet selector which then return to the updateValue function
        TextInputBottomSheet textInputBottomSheet = new TextInputBottomSheet(this,
                "SetActionsFragment",getString(R.string.export),getString(R.string.set),
                getString(R.string.set_saved_not_current),null,null,sets);
        textInputBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"TextInputBottomSheet");
    }

    public void updateValue(String setName) {
        if (setName!=null && !setName.isEmpty()) {
            // Set the "whattodo" to let the export fragment know we are exporting a set
            mainActivityInterface.setWhattodo("exportset:"+setName);
            mainActivityInterface.navigateToFragment("opensongapp://settings/actions/export",0);
        }
    }

}
