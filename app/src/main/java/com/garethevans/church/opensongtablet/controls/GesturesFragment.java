package com.garethevans.church.opensongtablet.controls;

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

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.appdata.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.appdata.ExposedDropDownSelection;
import com.garethevans.church.opensongtablet.databinding.SettingsGesturesBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;

import java.util.ArrayList;

public class GesturesFragment extends Fragment {

    private SettingsGesturesBinding myView;
    private Preferences preferences;
    private MainActivityInterface mainActivityInterface;
    private Gestures gestures;
    private ExposedDropDownSelection exposedDropDownSelection;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsGesturesBinding.inflate(inflater,container,false);
        mainActivityInterface.updateToolbar(null,requireContext().getString(R.string.custom_gestures));

        // Set up the helpers
        setupHelpers();

        // Set dropDowns
        new Thread(() -> getActivity().runOnUiThread(this::setupDropDowns)).start();

        return myView.getRoot();
    }

    private void setupHelpers() {
        preferences = mainActivityInterface.getPreferences();
        gestures = mainActivityInterface.getGestures();
        exposedDropDownSelection = new ExposedDropDownSelection();
    }

    private void setupDropDowns() {
        // Get the arrays for the dropdowns
        ArrayList<String> availableDescriptions = gestures.getGestureDescriptions();
        ExposedDropDownArrayAdapter descriptionsAdapter = new ExposedDropDownArrayAdapter(requireContext(), R.layout.exposed_dropdown, availableDescriptions);
        myView.doubleTap.setAdapter(descriptionsAdapter);
        myView.longPress.setAdapter(descriptionsAdapter);

        // Set the initial values
        myView.doubleTap.setText(gestures.getDescriptionFromGesture(gestures.getDoubleTap()));
        myView.longPress.setText(gestures.getDescriptionFromGesture(gestures.getLongPress()));

        // Set the listeners
        myView.doubleTap.addTextChangedListener(new MyTextWatcher("doubleTap"));
        myView.doubleTap.addTextChangedListener(new MyTextWatcher("longPress"));

        // Set the position in the list to the chosen value
        exposedDropDownSelection.keepSelectionPosition(myView.doubleTapLayout,myView.doubleTap, gestures.getGestureDescriptions());
        exposedDropDownSelection.keepSelectionPosition(myView.longPressLayout,myView.longPress, gestures.getGestureDescriptions());
    }

    private class MyTextWatcher implements TextWatcher {
        String which;
        MyTextWatcher(String which) {
            this.which = which;
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            String mydescription;
            if (which.equals("doubleTap")) {
                mydescription = myView.doubleTap.getText().toString();
            } else {
                mydescription = myView.longPress.getText().toString();
            }
            String mygesture = gestures.getGestureFromDescription(mydescription);
            gestures.setPreferences(requireContext(),preferences,which,mygesture);
        }
    }
}