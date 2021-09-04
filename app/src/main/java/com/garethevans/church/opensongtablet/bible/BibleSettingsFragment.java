package com.garethevans.church.opensongtablet.bible;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.BibleSettingsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class BibleSettingsFragment extends Fragment {

    private BibleSettingsBinding myView;
    private MainActivityInterface mainActivityInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BibleSettingsBinding.inflate(inflater, container, false);

        mainActivityInterface.updateToolbar(getString(R.string.bible));

        // Set up listeners
        setupListeners();

        return myView.getRoot();
    }

    private void setupListeners() {
        myView.downloadBible.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null, R.id.bibleDownloadFragment));
        myView.bibleOffline.setOnClickListener(v -> {
            BibleOfflineBottomSheet bibleOfflineBottomSheet = new BibleOfflineBottomSheet();
            bibleOfflineBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"bibleOffLineBottomSheet");
            mainActivityInterface.navHome();
        });
        myView.bibleGateway.setOnClickListener(v -> {
            BibleGatewayBottomSheet bibleGatewayBottomSheet = new BibleGatewayBottomSheet();
            bibleGatewayBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"bibleGatewayBottomSheet");
            mainActivityInterface.navHome();
        });
    }
}
