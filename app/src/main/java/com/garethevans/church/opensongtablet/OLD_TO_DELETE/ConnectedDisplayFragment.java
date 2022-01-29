package com.garethevans.church.opensongtablet.OLD_TO_DELETE;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.databinding.SettingsConnectedDisplayBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class ConnectedDisplayFragment extends Fragment {

    private final String TAG = "ConnectedDisplayFrag";
    private SettingsConnectedDisplayBinding myView;
    private MainActivityInterface mainActivityInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsConnectedDisplayBinding.inflate(inflater,container,false);

        // Update views
        updateViews();

        return myView.getRoot();
    }

    private void updateViews() {
        String pickThis = mainActivityInterface.getPreferences().getMyPreferenceString(requireContext(),
                "backgroundToUse", "img1");
    }

}
