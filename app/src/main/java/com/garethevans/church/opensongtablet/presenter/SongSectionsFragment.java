package com.garethevans.church.opensongtablet.presenter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.garethevans.church.opensongtablet.databinding.ModePresenterSongSectionsBinding;
import com.garethevans.church.opensongtablet.interfaces.DisplayInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class SongSectionsFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private DisplayInterface displayInterface;
    private ModePresenterSongSectionsBinding myView;
    private SongSectionsAdapter songSectionsAdapter;
    private final String TAG = "SongSectionsFragment";

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = getActivity().getWindow();
        if (w!=null) {
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }
    }

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
        myView.songInfo.minifyLayout(false);

        songSectionsAdapter = new SongSectionsAdapter(requireContext(),mainActivityInterface,this,
                displayInterface);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        myView.recyclerView.setLayoutManager(linearLayoutManager);
        myView.recyclerView.setAdapter(songSectionsAdapter);

        showSongInfo();

        return myView.getRoot();
    }

    public void showSongInfo() {
        if (myView!=null) {
            myView.songInfo.setSongTitle(mainActivityInterface.getSong().getTitle());
            myView.songInfo.setSongAuthor(mainActivityInterface.getSong().getAuthor());
            myView.songInfo.setSongCopyright(mainActivityInterface.getSong().getCopyright());
            songSectionsAdapter.setSelectedPosition(-1);
            songSectionsAdapter.buildSongSections();
        }
    }

    // From edited content via TextInputBottomSheet
    public void updateValue(String content) {
        songSectionsAdapter.setSectionEdited(content);
    }
}
