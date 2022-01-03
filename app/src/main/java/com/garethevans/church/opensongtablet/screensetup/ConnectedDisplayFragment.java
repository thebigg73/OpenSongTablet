package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.request.RequestOptions;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.GlideApp;
import com.garethevans.church.opensongtablet.databinding.SettingsConnectedDisplayBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class ConnectedDisplayFragment extends Fragment {

    private final String TAG = "ConnectedDisplayFrag";
    private String pickThis;
    private SettingsConnectedDisplayBinding myView;
    private MainActivityInterface mainActivityInterface;
    private Uri image1, image2, video1, video2;

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


        pickThis = mainActivityInterface.getPreferences().getMyPreferenceString(requireContext(),
                "backgroundToUse", "img1");





    }

}
