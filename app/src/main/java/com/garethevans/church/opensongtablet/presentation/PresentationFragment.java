package com.garethevans.church.opensongtablet.presentation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.StaticVariables;
import com.garethevans.church.opensongtablet.databinding.FragmentPresentationBinding;


public class PresentationFragment extends Fragment {

    private FragmentPresentationBinding myView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        StaticVariables.homeFragment = true;  // Set to true for Performance/Stage/Presentation only

        myView = FragmentPresentationBinding.inflate(inflater, container, false);
        View root = myView.getRoot();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}
