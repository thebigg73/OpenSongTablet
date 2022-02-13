package com.garethevans.church.opensongtablet.presenter;

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

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.ModePresenterSongSectionsBinding;
import com.garethevans.church.opensongtablet.interfaces.DisplayInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

public class SongSectionsFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private DisplayInterface displayInterface;
    private ModePresenterSongSectionsBinding myView;
    private PresenterFragment presenterFragment;
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
        myView.songInfo.setupLayout(mainActivityInterface,true);

        // Set the presentation order
        myView.presentationOrder.setChecked(mainActivityInterface.getPresenterSettings().getUsePresentationOrder());
        myView.presentationOrder.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireContext(),"usePresentationOrder",b);
            mainActivityInterface.getPresenterSettings().setUsePresentationOrder(b);
            mainActivityInterface.doSongLoad(mainActivityInterface.getSong().getFolder(),
                    mainActivityInterface.getSong().getFilename(),true);
        });
        updatePresentationOrder();


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        myView.recyclerView.setLayoutManager(linearLayoutManager);
        myView.recyclerView.setAdapter(mainActivityInterface.getPresenterSettings().getSongSectionsAdapter());

        showSongInfo();

        mainActivityInterface.updateFragment("presenterFragment_showCase",null,null);

        return myView.getRoot();
    }

    public void showSongInfo() {
        if (myView!=null && mainActivityInterface!=null && mainActivityInterface.getPresenterSettings()!=null &&
        mainActivityInterface.getPresenterSettings().getSongSectionsAdapter()!=null) {
            myView.songInfo.setSongTitle(mainActivityInterface.getSong().getTitle());
            myView.songInfo.setSongAuthor(mainActivityInterface.getSong().getAuthor());
            myView.songInfo.setSongCopyright(mainActivityInterface.getSong().getCopyright());
            myView.songInfo.setSongCCLI(mainActivityInterface.getSong().getCcli());
            mainActivityInterface.getPresenterSettings().getSongSectionsAdapter().setSelectedPosition(-1);
            mainActivityInterface.getPresenterSettings().getSongSectionsAdapter().buildSongSections();
            myView.songInfo.setOnLongClickListener(view -> {
                mainActivityInterface.navigateToFragment("opensongapp://settings/edit",0);
                return false;
            });
            updatePresentationOrder();
        }

    }

    public void updatePresentationOrder() {
        Log.d(TAG,"mainActivityInterface:" + mainActivityInterface);
        Log.d(TAG,"mainActivityInterface.getSong():" + mainActivityInterface.getSong());
        if (mainActivityInterface.getSong().getPresentationorder()!=null &&
            !mainActivityInterface.getSong().getPresentationorder().isEmpty()) {
            myView.presentationOrder.setHint(mainActivityInterface.getSong().getPresentationorder());
        } else {
            myView.presentationOrder.setHint(getString(R.string.is_not_set));
        }
    }

    public void selectSection(int newPosition) {
        if (mainActivityInterface.getPresenterSettings().getSongSectionsAdapter().getItemCount()>newPosition) {
            int oldPosition = mainActivityInterface.getPresenterSettings().getSongSectionsAdapter().getSelectedPosition();
            mainActivityInterface.getPresenterSettings().getSongSectionsAdapter().setSelectedPosition(newPosition);
            mainActivityInterface.getPresenterSettings().getSongSectionsAdapter().notifyItemChanged(oldPosition);
            mainActivityInterface.getPresenterSettings().getSongSectionsAdapter().notifyItemChanged(newPosition);
        }
    }

    public void updateAllButtons() {
        if (mainActivityInterface.getPresenterSettings().getSongSectionsAdapter().getItemCount()>0) {
            mainActivityInterface.getPresenterSettings().getSongSectionsAdapter().
                    notifyItemRangeChanged(0, mainActivityInterface.getPresenterSettings().getSongSectionsAdapter().getItemCount());
        }
    }

    // From edited content via TextInputBottomSheet
    public void updateValue(String content) {
        mainActivityInterface.getPresenterSettings().getSongSectionsAdapter().setSectionEdited(content);
    }

    public void doScrollTo(int thisPos) {
        ((LinearLayoutManager)myView.recyclerView.getLayoutManager()).scrollToPositionWithOffset(thisPos,0);
    }

    public void showTutorial(ArrayList<View> viewsToHighlight) {
        // The presenter fragment has sent the main parent views
        // Add these ones and showcase
        Log.d(TAG,"showTutorial");
        if (myView!=null) {
            viewsToHighlight.add(myView.songInfo);
            viewsToHighlight.add(myView.recyclerView);
            mainActivityInterface.showTutorial("presenterSongs", viewsToHighlight);
        }
    }

}
