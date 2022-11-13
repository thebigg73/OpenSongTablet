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
import com.garethevans.church.opensongtablet.customslides.ImageSlideAdapter;
import com.garethevans.church.opensongtablet.databinding.ModePresenterSongSectionsBinding;
import com.garethevans.church.opensongtablet.interfaces.DisplayInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.pdf.PDFPageAdapter;

import java.util.ArrayList;

public class SongSectionsFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private DisplayInterface displayInterface;
    private ModePresenterSongSectionsBinding myView;
    @SuppressWarnings({"FieldCanBeLocal","unused"})
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
        displayInterface = (DisplayInterface) context;
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

        myView.recyclerView.setItemAnimator(null);
        myView.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        myView.recyclerView.setAdapter(mainActivityInterface.getPresenterSettings().getSongSectionsAdapter());

        showSongInfo();

        mainActivityInterface.updateFragment("presenterFragment_showCase",null,null);

        return myView.getRoot();
    }

    public void showSongInfo() {
        myView.songInfo.setCapo(null);  // Don't need to show this here
        if (mainActivityInterface!=null &&
                mainActivityInterface.getSong()!=null) {
            myView.songInfo.setSongTitle(mainActivityInterface.getSong().getTitle());
            myView.songInfo.setSongAuthor(mainActivityInterface.getSong().getAuthor());
            myView.songInfo.setSongCopyright(mainActivityInterface.getSong().getCopyright());
            myView.songInfo.setSongCCLI(mainActivityInterface.getSong().getCcli());
            myView.songInfo.setOnLongClickListener(view -> {
                mainActivityInterface.navigateToFragment(getString(R.string.deeplink_edit), 0);
                return false;
            });
            if (mainActivityInterface.getSong().getFiletype().equals("PDF") &&
                    android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                PDFPageAdapter pdfPageAdapter = new PDFPageAdapter(requireContext(),
                        mainActivityInterface, displayInterface, 600, 800);

                Log.d(TAG, "pages:" + pdfPageAdapter.getItemCount());
                mainActivityInterface.getSong().setPdfPageCount(pdfPageAdapter.getItemCount());
                Log.d(TAG, "heights" + pdfPageAdapter.getHeights());

                myView.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                myView.recyclerView.setAdapter(pdfPageAdapter);


            } else if (mainActivityInterface.getSong().getFiletype().equals("IMG")) {
                ImageAdapter imageAdapter = new ImageAdapter(requireContext(),this,mainActivityInterface,displayInterface,600,800);
                myView.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                myView.recyclerView.setAdapter(imageAdapter);

            } else if (mainActivityInterface.getSong().getFolder().contains("Images/")) {
                ImageSlideAdapter imageSlideAdapter = new ImageSlideAdapter(requireContext(),mainActivityInterface,displayInterface,600,800);
                myView.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                myView.recyclerView.setAdapter(imageSlideAdapter);

            } else {
                // Standard XML file
                myView.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                myView.recyclerView.setAdapter(mainActivityInterface.getPresenterSettings().getSongSectionsAdapter());

                if (myView != null && mainActivityInterface != null && mainActivityInterface.getPresenterSettings() != null &&
                        mainActivityInterface.getPresenterSettings().getSongSectionsAdapter() != null) {
                    mainActivityInterface.getPresenterSettings().getSongSectionsAdapter().setSelectedPosition(-1);
                    mainActivityInterface.getPresenterSettings().getSongSectionsAdapter().buildSongSections();
                    updatePresentationOrder();
                    updateAllButtons();
                }
            }
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
