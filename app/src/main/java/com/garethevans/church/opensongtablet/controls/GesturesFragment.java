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

import java.util.ArrayList;

public class GesturesFragment extends Fragment {

    private SettingsGesturesBinding myView;
    private MainActivityInterface mainActivityInterface;
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
        mainActivityInterface.updateToolbar(getString(R.string.custom_gestures));

        // Set up the helpers
        setupHelpers();

        // Set dropDowns
        new Thread(() -> getActivity().runOnUiThread(this::setupDropDowns)).start();

        return myView.getRoot();
    }

    private void setupHelpers() {
        exposedDropDownSelection = new ExposedDropDownSelection();
    }

    private void setupDropDowns() {
        // Get the arrays for the dropdowns
        ArrayList<String> availableDescriptions = mainActivityInterface.getGestures().getGestureDescriptions();
        ExposedDropDownArrayAdapter descriptionsAdapter = new ExposedDropDownArrayAdapter(requireContext(), R.layout.exposed_dropdown, availableDescriptions);
        myView.doubleTap.setAdapter(descriptionsAdapter);
        myView.longPress.setAdapter(descriptionsAdapter);

        // Set the initial values
        myView.doubleTap.setText(mainActivityInterface.getGestures().getDescriptionFromGesture(mainActivityInterface.getGestures().getDoubleTap()));
        myView.longPress.setText(mainActivityInterface.getGestures().getDescriptionFromGesture(mainActivityInterface.getGestures().getLongPress()));

        // Set the listeners
        myView.doubleTap.addTextChangedListener(new MyTextWatcher("doubleTap"));
        myView.doubleTap.addTextChangedListener(new MyTextWatcher("longPress"));

        // Set the position in the list to the chosen value
        exposedDropDownSelection.keepSelectionPosition(myView.doubleTapLayout,myView.doubleTap, mainActivityInterface.getGestures().getGestureDescriptions());
        exposedDropDownSelection.keepSelectionPosition(myView.longPressLayout,myView.longPress, mainActivityInterface.getGestures().getGestureDescriptions());
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
            String mygesture = mainActivityInterface.getGestures().getGestureFromDescription(mydescription);
            mainActivityInterface.getGestures().setPreferences(requireContext(),mainActivityInterface,which,mygesture);
        }
    }
}