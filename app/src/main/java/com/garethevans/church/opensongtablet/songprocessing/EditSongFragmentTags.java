package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.databinding.FragmentEditSong3Binding;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.songsandsetsmenu.SongListAdapter;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;

public class EditSongFragmentTags extends Fragment {

    // The helper classes used
    private Preferences preferences;
    private SQLiteHelper sqLiteHelper;
    private SongListAdapter songListAdapter;

    FragmentEditSong3Binding myView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = FragmentEditSong3Binding.inflate(inflater, container, false);

        return myView.getRoot();
    }
}