package com.garethevans.church.opensongtablet.controls;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.SettingsHotZonesBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HotZonesSettingsFragment extends Fragment {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "HotZonesSettings";

    private SettingsHotZonesBinding myView;
    private MainActivityInterface mainActivityInterface;
    private String hot_zones_string="", website_hot_zones_string="";
    private String webAddress;

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(hot_zones_string);
        mainActivityInterface.updateToolbarHelp(webAddress);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsHotZonesBinding.inflate(inflater,container,false);

        prepareStrings();

        webAddress = website_hot_zones_string;

        // Set dropDowns
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(this::setupDropDowns);
        });

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            hot_zones_string = getString(R.string.hot_zones);
            website_hot_zones_string = getString(R.string.website_hot_zones);
        }
    }

    private void setupDropDowns() {
        // Get the arrays for the dropdowns
        ArrayList<String> availableDescriptions = mainActivityInterface.getCommonControls().getGestureDescriptions();
        if (getContext()!=null) {
            ExposedDropDownArrayAdapter adapterTLShort = new ExposedDropDownArrayAdapter(getContext(), myView.hotZoneTLShort, R.layout.view_exposed_dropdown_item, availableDescriptions);
            ExposedDropDownArrayAdapter adapterTLLong = new ExposedDropDownArrayAdapter(getContext(), myView.hotZoneTLLong, R.layout.view_exposed_dropdown_item, availableDescriptions);
            ExposedDropDownArrayAdapter adapterTCShort = new ExposedDropDownArrayAdapter(getContext(), myView.hotZoneTCShort, R.layout.view_exposed_dropdown_item, availableDescriptions);
            ExposedDropDownArrayAdapter adapterTCLong = new ExposedDropDownArrayAdapter(getContext(), myView.hotZoneTCLong, R.layout.view_exposed_dropdown_item, availableDescriptions);
            ExposedDropDownArrayAdapter adapterBCShort = new ExposedDropDownArrayAdapter(getContext(), myView.hotZoneBCShort, R.layout.view_exposed_dropdown_item, availableDescriptions);
            ExposedDropDownArrayAdapter adapterBCLong = new ExposedDropDownArrayAdapter(getContext(), myView.hotZoneBCLong, R.layout.view_exposed_dropdown_item, availableDescriptions);
            myView.hotZoneTLShort.setAdapter(adapterTLShort);
            myView.hotZoneTCShort.setAdapter(adapterTCShort);
            myView.hotZoneBCShort.setAdapter(adapterBCShort);
            myView.hotZoneTLLong.setAdapter(adapterTLLong);
            myView.hotZoneTCLong.setAdapter(adapterTCLong);
            myView.hotZoneBCLong.setAdapter(adapterBCLong);

            // Set the current values
            myView.hotZoneTLShort.setText(mainActivityInterface.getCommonControls().getDescriptionFromCode(mainActivityInterface.getHotZones().getHotZoneTopLeftShort()));
            myView.hotZoneTCShort.setText(mainActivityInterface.getCommonControls().getDescriptionFromCode(mainActivityInterface.getHotZones().getHotZoneTopCenterShort()));
            myView.hotZoneBCShort.setText(mainActivityInterface.getCommonControls().getDescriptionFromCode(mainActivityInterface.getHotZones().getHotZoneBottomCenterShort()));
            myView.hotZoneTLLong.setText(mainActivityInterface.getCommonControls().getDescriptionFromCode(mainActivityInterface.getHotZones().getHotZoneTopLeftLong()));
            myView.hotZoneTCLong.setText(mainActivityInterface.getCommonControls().getDescriptionFromCode(mainActivityInterface.getHotZones().getHotZoneTopCenterLong()));
            myView.hotZoneBCLong.setText(mainActivityInterface.getCommonControls().getDescriptionFromCode(mainActivityInterface.getHotZones().getHotZoneBottomCenterLong()));

            // Set the listeners
            myView.hotZoneTLShort.addTextChangedListener(new MyTextWatcher("hotZoneTopLeftShort"));
            myView.hotZoneTCShort.addTextChangedListener(new MyTextWatcher("hotZoneTopCenterShort"));
            myView.hotZoneBCShort.addTextChangedListener(new MyTextWatcher("hotZoneBottomCenterShort"));
            myView.hotZoneTLLong.addTextChangedListener(new MyTextWatcher("hotZoneTopLeftLong"));
            myView.hotZoneTCLong.addTextChangedListener(new MyTextWatcher("hotZoneTopCenterLong"));
            myView.hotZoneBCLong.addTextChangedListener(new MyTextWatcher("hotZoneBottomCenterLong"));
        }
    }

    private class MyTextWatcher implements TextWatcher {

        private final String prefName;
        MyTextWatcher(String prefName) {
            this.prefName = prefName;
        }
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            String code = mainActivityInterface.getCommonControls().getCodeFromDescription(editable.toString());
            // Update the preferences
            switch (prefName) {
                case "hotZoneTopLeftShort":
                    mainActivityInterface.getHotZones().setHotZoneTopLeftShort(code);
                    break;
                case "hotZoneTopLeftLong":
                    mainActivityInterface.getHotZones().setHotZoneTopLeftLong(code);
                    break;
                case "hotZoneTopCenterShort":
                    mainActivityInterface.getHotZones().setHotZoneTopCenterShort(code);
                    break;
                case "hotZoneTopCenterLong":
                    mainActivityInterface.getHotZones().setHotZoneTopCenterLong(code);
                    break;
                case "hotZoneBottomCenterShort":
                    mainActivityInterface.getHotZones().setHotZoneBottomCenterShort(code);
                    break;
                case "hotZoneBottomCenterLong":
                    mainActivityInterface.getHotZones().setHotZoneBottomCenterLong(code);
                    break;
            }
        }
    }
}
