package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.garethevans.church.opensongtablet.databinding.BottomSheetEditSongThemeBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ThemesBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetEditSongThemeBinding myView;
    private final Fragment callingFragment;
    private final String fragName;
    private MainActivityInterface mainActivityInterface;
    private ThemesAdapter themesAdapter;

    ThemesBottomSheet(Fragment callingFragment, String fragName) {
        this.callingFragment = callingFragment;
        this.fragName = fragName;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetEditSongThemeBinding.inflate(inflater, container, false);

        myView.dialogHeading.setClose(this);

        setCurrentTags();
        return myView.getRoot();
    }

    private void setCurrentTags() {
        // Update the recycler view
        themesAdapter = new ThemesAdapter(requireContext(),mainActivityInterface,
                requireActivity().getSupportFragmentManager(),callingFragment,fragName);
        myView.currentTags.setAdapter(themesAdapter);
        myView.currentTags.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    public void deleteTags(int position) {
        // The tags have been deleted, so update the recycler view
        themesAdapter.notifyItemRemoved(position);
    }

}
