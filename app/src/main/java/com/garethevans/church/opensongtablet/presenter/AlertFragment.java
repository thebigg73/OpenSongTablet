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

import com.garethevans.church.opensongtablet.databinding.ModePresenterAlertBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class AlertFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private ModePresenterAlertBinding myView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = ModePresenterAlertBinding.inflate(inflater, container, false);

        setValues();

        setListeners();

        return myView.getRoot();
    }

    private void setValues() {
        myView.alertText.setText(mainActivityInterface.getPreferences().getMyPreferenceString(requireContext(),"presoAlertText",""));
        myView.alertSwitch.setChecked(mainActivityInterface.getPresenterSettings().getAlertOn());
    }

    private void setListeners() {
        myView.alertText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                mainActivityInterface.getPreferences().setMyPreferenceString(requireContext(), "presoAlertText",editable.toString());
                mainActivityInterface.updateDisplay("alert");
            }
        });
        myView.alertSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getPresenterSettings().setAlertOn(b);
            mainActivityInterface.updateDisplay("alert");
        });
    }
}
