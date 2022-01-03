package com.garethevans.church.opensongtablet.presenter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.garethevans.church.opensongtablet.databinding.ModePresenterSettingsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class SettingsFragment extends Fragment {

    private final String TAG = "SettingsFragment";
    private MainActivityInterface mainActivityInterface;
    private ModePresenterSettingsBinding myView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = ModePresenterSettingsBinding.inflate(inflater,container,false);

        // Update the currently chosen logo and background
        mainActivityInterface.getPresenterSettings().getPreferences(requireContext(),mainActivityInterface);
        updateLogo();
        updateBackground();

        // Set the listeners
        setListeners();

        return myView.getRoot();
    }

    public void updateLogo() {
        Log.d(TAG,"updateLogo()");
        // Get the current logo and preview in the button
        RequestOptions options = new RequestOptions().override(128, 72).centerInside();
        Glide.with(requireContext()).load(mainActivityInterface.getPresenterSettings().getLogo()).apply(options).into(myView.currentLogo);
    }

    public void updateBackground() {
        Log.d(TAG,"updateBackground()");
        // Get the current backgrounds and update the chosen one into the button
        RequestOptions options = new RequestOptions().override(128, 72).centerInside();
        switch (mainActivityInterface.getPresenterSettings().getBackgroundToUse()) {
            case "img1":
                Glide.with(requireContext()).load(mainActivityInterface.getPresenterSettings().getBackgroundImage1()).apply(options).into(myView.currentBackground);
                myView.videoBackgroundIcon.hide();
                Log.d(TAG,"should update to img1");
                break;
            case "img2":
                Glide.with(requireContext()).load(mainActivityInterface.getPresenterSettings().getBackgroundImage2()).apply(options).into(myView.currentBackground);
                myView.videoBackgroundIcon.hide();
                break;
            case "vid1":
                Glide.with(requireContext()).load(mainActivityInterface.getPresenterSettings().getBackgroundVideo1()).apply(options).into(myView.currentBackground);
                myView.videoBackgroundIcon.show();
                break;
            case "vid2":
                Glide.with(requireContext()).load(mainActivityInterface.getPresenterSettings().getBackgroundVideo2()).apply(options).into(myView.currentBackground);
                myView.videoBackgroundIcon.show();
                break;
            case "color":
                myView.currentBackground.setBackgroundColor(mainActivityInterface.getPresenterSettings().getBackgroundColor());
                //Glide.with(requireContext()).load(ContextCompat.getDrawable(requireContext(), R.drawable.rectangle)).apply(options).into(myView.currentBackground);
                //myView.currentBackground.setColorFilter(mainActivityInterface.getPresenterSettings().getBackgroundColor(), android.graphics.PorterDuff.Mode.SRC_ATOP);
                myView.videoBackgroundIcon.hide();
        }
    }

    private void setListeners() {
        myView.currentBackground.setOnClickListener(view -> {
            ImageChooserBottomSheet imageChooserBottomSheet = new ImageChooserBottomSheet(this,"presenterFragmentSettings");
            imageChooserBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"ImageChooserBottomSheet");
        });
    }


}
