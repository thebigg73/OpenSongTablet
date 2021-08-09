package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.BottomSheetEditSongThemeBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ThemesBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetEditSongThemeBinding myView;
    private final Fragment callingFragment;
    private final String fragName;
    private MainActivityInterface mainActivityInterface;
    private ThemesAdapter themesAdapter;
    private final String TAG = "ThemesBottomSheet";

    ThemesBottomSheet(Fragment callingFragment, String fragName) {
        this.callingFragment = callingFragment;
        this.fragName = fragName;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = requireActivity().getWindow();
        if (w!=null) {
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetEditSongThemeBinding.inflate(inflater, container, false);

        myView.dialogHeading.setClose(this);

        setCurrentTags();

        // Set the listener for inserting a new tag
        // This gets assigned to this song
        myView.insertTag.setOnClickListener(v -> insertTag());

        return myView.getRoot();
    }

    private void setCurrentTags() {
        // Update the recycler view
        themesAdapter = new ThemesAdapter(requireContext(),mainActivityInterface,
                requireActivity().getSupportFragmentManager(),callingFragment,fragName);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        myView.currentTags.setLayoutManager(new LinearLayoutManager(requireContext()));
        myView.currentTags.setAdapter(themesAdapter);
        }

    public void insertTag() {
        // Check this song doesn't have this tag already (meaning it's already in the list)
        String themeString = mainActivityInterface.getTempSong().getTheme().trim();
        String newThemeString = myView.newTag.getText().toString().trim();
        Log.d(TAG,"themeString: "+themeString);
        if (!themeString.contains(newThemeString + ";") && !themeString.endsWith(newThemeString)) {
            themeString = themeString + "; " + newThemeString;
            themeString = mainActivityInterface.getProcessSong().tidyThemeString(themeString);
            mainActivityInterface.getTempSong().setTheme(themeString);
            themesAdapter.insertThemeTag(newThemeString);
            myView.currentTags.smoothScrollToPosition(0);
        } else {
            mainActivityInterface.getShowToast().doIt(requireContext(),getString(R.string.theme_exists));
        }
    }
    public void deleteTags(int position) {
        // The tags have been deleted, so update the recycler view
        themesAdapter.notifyItemRemoved(position);
    }


}
