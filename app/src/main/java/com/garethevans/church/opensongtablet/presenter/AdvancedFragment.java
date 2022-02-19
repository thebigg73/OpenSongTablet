package com.garethevans.church.opensongtablet.presenter;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.databinding.ModePresenterAdvancedBinding;
import com.garethevans.church.opensongtablet.interfaces.DisplayInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.slider.Slider;

public class AdvancedFragment extends Fragment {

    private ModePresenterAdvancedBinding myView;
    private MainActivityInterface mainActivityInterface;
    private DisplayInterface displayInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        displayInterface = (DisplayInterface) context;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = ModePresenterAdvancedBinding.inflate(inflater,container,false);

        setValues();

        setListeners();

        return myView.getRoot();
    }

    private void setValues() {
        myView.alertText.setText(mainActivityInterface.getPreferences().getMyPreferenceString(requireContext(),"presoAlertText",""));
        myView.alertSwitch.setChecked(mainActivityInterface.getPresenterSettings().getAlertOn());
        myView.presoAlertTextSize.setValue((int)mainActivityInterface.getPresenterSettings().getPresoAlertTextSize());
        myView.presoAlertTextSize.setHint((int)mainActivityInterface.getPresenterSettings().getPresoAlertTextSize() + "sp");
        myView.presoAlertTextSize.setLabelFormatter(value -> ((int)value)+"sp");
    }

    private void setListeners() {
        myView.displaySettings.setOnClickListener(view -> mainActivityInterface.navigateToFragment("opensongapp://settings/display/connected",0));
        myView.alertText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = "";
                if (editable!=null) {
                    text = editable.toString();
                }
                mainActivityInterface.getPreferences().setMyPreferenceString(requireContext(), "presoAlertText",text);
                mainActivityInterface.getPresenterSettings().setPresoAlertText(text);
                displayInterface.updateDisplay("updateAlert");
            }
        });
        myView.alertSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getPresenterSettings().setAlertOn(b);
            displayInterface.updateDisplay("showAlert");
        });
        myView.presoAlertTextSize.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) { }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                mainActivityInterface.getPreferences().setMyPreferenceFloat(requireContext(),
                        "presoAlertTextSize",slider.getValue());
                mainActivityInterface.getPresenterSettings().setPresoAlertTextSize(slider.getValue());
                displayInterface.updateDisplay("updateAlert");
            }
        });
        myView.presoAlertTextSize.addOnChangeListener((slider, value, fromUser) -> myView.presoAlertTextSize.setHint((int)slider.getValue() + "sp"));
    }

}
