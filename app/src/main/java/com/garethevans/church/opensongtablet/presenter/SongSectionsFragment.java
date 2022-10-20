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
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

public class SongSectionsFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private ModePresenterSongSectionsBinding myView;
    private final String TAG = "SongSectionsFragment";

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = requireActivity().getWindow();
        if (w!=null) {
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = ModePresenterSongSectionsBinding.inflate(inflater,container,false);

        // Set up song info layout to only show minimal info in simple format
        myView.songInfo.setupLayout(requireContext(),mainActivityInterface,true);

        // Set the presentation order
        myView.presentationOrder.setChecked(mainActivityInterface.getPresenterSettings().getUsePresentationOrder());
        myView.presentationOrder.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("usePresentationOrder",b);
            mainActivityInterface.getPresenterSettings().setUsePresentationOrder(b);
            mainActivityInterface.updateFragment("presenterFragmentSongSections",getParentFragment(),null);
        });
        updatePresentationOrder();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        myView.recyclerView.setItemAnimator(null);
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
                mainActivityInterface.navigateToFragment(getString(R.string.deeplink_edit),0);
                return false;
            });
            updatePresentationOrder();
            updateAllButtons();
        }
    }

    public void updatePresentationOrder() {
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
            mainActivityInterface.getPresenterSettings().getSongSectionsAdapter().notifyItemChanged(oldPosition,"colorchange");
            mainActivityInterface.getPresenterSettings().getSongSectionsAdapter().notifyItemChanged(newPosition,"colorchange");
        }
    }

    public void updateAllButtons() {
        if (mainActivityInterface.getPresenterSettings().getSongSectionsAdapter()!=null) {
            myView.recyclerView.removeAllViews();
        /*mainActivityInterface.setSectionViews(mainActivityInterface.getProcessSong().setSongInLayout(
                mainActivityInterface.getSong(), false, true));
        */
            mainActivityInterface.getPresenterSettings().getSongSectionsAdapter().buildSongSections();
            if (mainActivityInterface.getPresenterSettings().getSongSectionsAdapter().getItemCount() > 0) {
                mainActivityInterface.getPresenterSettings().getSongSectionsAdapter().
                        notifyItemRangeChanged(0, mainActivityInterface.getPresenterSettings().getSongSectionsAdapter().getItemCount());
            }
        }
    }

    // From edited content via TextInputBottomSheet
    public void updateValue(String content) {
        mainActivityInterface.getPresenterSettings().getSongSectionsAdapter().setSectionEditedContent(content);
    }

    public void showTutorial(ArrayList<View> viewsToHighlight) {
        // The presenter fragment has sent the main parent views
        // Add these ones and showcase
        if (myView!=null) {
            viewsToHighlight.add(myView.songInfo);
            viewsToHighlight.add(myView.recyclerView);
            mainActivityInterface.showTutorial("presenterSongs", viewsToHighlight);
        }
    }

}
