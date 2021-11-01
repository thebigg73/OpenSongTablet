package com.garethevans.church.opensongtablet.presenter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.databinding.ModePresenterSongSectionsBinding;
import com.garethevans.church.opensongtablet.interfaces.DisplayInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class SongSectionsFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private DisplayInterface displayInterface;
    private ModePresenterSongSectionsBinding myView;
    private final String TAG = "SongSectionsFragment";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        displayInterface = (DisplayInterface) context;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = ModePresenterSongSectionsBinding.inflate(inflater,container,false);

        // Set up song info layout to only show minimal info in simple format
        myView.songInfo.minifyLayout();

        showSongInfo();

        return myView.getRoot();
    }

    public void showSongInfo() {
        if (myView!=null) {
            Log.d(TAG, "showSongInfo() called");
            Log.d(TAG,"folder: "+mainActivityInterface.getSong().getFolder());
            Log.d(TAG, "filename: "+mainActivityInterface.getSong().getFilename());
            myView.songInfo.setSongTitle(mainActivityInterface.getSong().getTitle());
            myView.songInfo.setSongAuthor(mainActivityInterface.getSong().getAuthor());
            myView.songInfo.setSongCopyright(mainActivityInterface.getSong().getCopyright());

            SongSectionsAdapter songSectionsAdapter = new SongSectionsAdapter(mainActivityInterface,displayInterface);
            myView.recyclerView.setAdapter(songSectionsAdapter);
        }
    }
}
