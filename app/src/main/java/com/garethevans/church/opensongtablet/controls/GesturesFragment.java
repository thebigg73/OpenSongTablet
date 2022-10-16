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
import com.garethevans.church.opensongtablet.databinding.SettingsGesturesBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GesturesFragment extends Fragment {

    private SettingsGesturesBinding myView;
    private MainActivityInterface mainActivityInterface;

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
        mainActivityInterface.updateToolbarHelp(getString(R.string.website_custom_gestures));

        // Set dropDowns
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(this::setupDropDowns);
        });

        return myView.getRoot();
    }

    private void setupDropDowns() {
        // Get the arrays for the dropdowns
        ArrayList<String> availableDescriptions = mainActivityInterface.getGestures().getGestureDescriptions();
        ExposedDropDownArrayAdapter descriptionsAdapter1 = new ExposedDropDownArrayAdapter(requireContext(), myView.doubleTap, R.layout.view_exposed_dropdown_item, availableDescriptions);
        ExposedDropDownArrayAdapter descriptionsAdapter2 = new ExposedDropDownArrayAdapter(requireContext(), myView.longPress, R.layout.view_exposed_dropdown_item, availableDescriptions);
        myView.doubleTap.setAdapter(descriptionsAdapter1);
        myView.longPress.setAdapter(descriptionsAdapter2);

        // Set the initial values
        myView.doubleTap.setText(mainActivityInterface.getGestures().getDescriptionFromGesture(mainActivityInterface.getGestures().getDoubleTap()));
        myView.longPress.setText(mainActivityInterface.getGestures().getDescriptionFromGesture(mainActivityInterface.getGestures().getLongPress()));

        // Set the listeners
        myView.doubleTap.addTextChangedListener(new MyTextWatcher("gestureDoubleTap"));
        myView.longPress.addTextChangedListener(new MyTextWatcher("gestureLongPress"));
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
            if (which.equals("gestureDoubleTap")) {
                mydescription = myView.doubleTap.getText().toString();
            } else {
                mydescription = myView.longPress.getText().toString();
            }
            String mygesture = mainActivityInterface.getGestures().getGestureFromDescription(mydescription);
            mainActivityInterface.getGestures().setPreferences(which,mygesture);
        }
    }
}