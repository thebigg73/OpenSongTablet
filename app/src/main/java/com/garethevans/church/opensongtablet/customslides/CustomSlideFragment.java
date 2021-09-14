package com.garethevans.church.opensongtablet.customslides;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsCustomSlideBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class CustomSlideFragment extends Fragment {

    private SettingsCustomSlideBinding myView;
    private MainActivityInterface mainActivityInterface;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsCustomSlideBinding.inflate(inflater, container, false);
        mainActivityInterface.updateToolbar(getString(R.string.custom_slide));
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        // Set up views
        setupViews();

        // Set up listeners
        setupListeners();

        return myView.getRoot();
    }

    private void setupViews() {
        mainActivityInterface.getProcessSong().editBoxToMultiline(myView.content);
        mainActivityInterface.getProcessSong().stretchEditBoxToLines(myView.content,8);
    }

    private void setupListeners() {
        // Add a new page to the content
        myView.addPage.setOnClickListener(v -> {
            String currentText = "";
            if (myView.content.getText()!=null) {
                currentText = myView.content.getText().toString();
            }
            // Add the new line text
            currentText = currentText + "\n---\n";
            myView.content.setText(currentText);
            myView.content.requestFocus();
            myView.content.setSelection(currentText.length());
        });
    }
}
