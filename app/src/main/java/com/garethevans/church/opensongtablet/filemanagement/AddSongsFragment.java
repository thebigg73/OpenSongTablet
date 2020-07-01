package com.garethevans.church.opensongtablet.filemanagement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.garethevans.church.opensongtablet.Preferences;
import com.garethevans.church.opensongtablet.SQLiteHelper;
import com.garethevans.church.opensongtablet.databinding.FragmentAddSongsBinding;
import com.garethevans.church.opensongtablet.songsandsets.SongListAdapter;

public class AddSongsFragment extends Fragment {

    // The helper classes used
    private Preferences preferences;
    private SQLiteHelper sqLiteHelper;
    private SongListAdapter songListAdapter;

    // The variable used in this fragment
    private FragmentAddSongsBinding myView;
    private NavController navController;
    private String whichMode;



    // The code to initialise this fragment
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        myView = FragmentAddSongsBinding.inflate(inflater, container, false);
        View root = myView.getRoot();

        navController = NavHostFragment.findNavController(this);

        // initialise helpers
        initialiseHelpers();

        // Set the mode we entered this fragment via
        whichMode = preferences.getMyPreferenceString(getActivity(),"whichMode","Performance");

        // Set listeners
        setUpListeners();

        return root;
    }


    // Finished with this view
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }


    // Getting the preferences and helpers ready
    private void initialiseHelpers() {
        preferences = new Preferences();
        sqLiteHelper = new SQLiteHelper(getActivity());
    }


    // Sor the view visibility, listeners, etc.
    private void setUpListeners() {
/*        myView.createSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("d","create");
            }
        });
        myView.importFromOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("d","online");
            }
        });
        myView.importFromFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("d","file");
            }
        });*/
        /*myView..setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("d","camera");
            }
        });*/
    }

}
