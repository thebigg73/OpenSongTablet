package com.garethevans.church.opensongtablet.setprocessing;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MyToolbar;
import com.garethevans.church.opensongtablet.databinding.SettingsSetImportBundleBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.customviews.MyTabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ImportSetBundleFragment extends Fragment {

    // This class is used to import an OpenSong Set Backup file (.ossb)
    // It will also deal with a JustChords set file (.justchords)

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "ImportSetBundleFragment";
    private MainActivityInterface mainActivityInterface;
    private String set_bundle_opensongapp_import="", website_set_import_bundle="",
            set_list="", included_songs="", set_bundle_justchords="";
    private SettingsSetImportBundleBinding myView;
    private ImportBundleViewPagerAdapter importBundleViewPagerAdapter;
    private ImportSetBundleSetFragment importSetBundleSetFragment;
    private ImportSetBundleSongFragment importSetBundleSongFragment;
    private ViewPager2 importSetBundleViewPager;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivityInterface = null;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = SettingsSetImportBundleBinding.inflate(inflater, container, false);

        myView.getRoot().setBackgroundColor(mainActivityInterface.getPalette().background);

        return myView.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        prepareStrings();
        if (mainActivityInterface.getWhattodo().equals("justchordsset")) {
            mainActivityInterface.updateToolbar(set_bundle_justchords);
        } else {
            mainActivityInterface.updateToolbar(set_bundle_opensongapp_import);
        }
        mainActivityInterface.updateToolbarHelp(website_set_import_bundle);
        setupViewPager();
    }

    private void prepareStrings() {
        if (getContext() != null) {
            set_bundle_opensongapp_import = getString(R.string.set_bundle_opensongapp_import);
            set_bundle_justchords = getString(R.string.set_bundle_justchords);
            website_set_import_bundle = getString(R.string.website_set_import_bundle);
            set_list = getString(R.string.set_list);
            included_songs = getString(R.string.included_songs);
        }
    }

    private void setupViewPager() {
        mainActivityInterface.getMainHandler().post(() -> {
            if (importBundleViewPagerAdapter == null) {
                importBundleViewPagerAdapter = new ImportBundleViewPagerAdapter(mainActivityInterface.getMyFragmentManager(), this.getLifecycle());
                importBundleViewPagerAdapter.createFragment(0);
            }
            if (importSetBundleSetFragment == null) {
                importSetBundleSetFragment = (ImportSetBundleSetFragment) importBundleViewPagerAdapter.menuFragments[0];
            }
            if (importSetBundleSongFragment == null) {
                importSetBundleSongFragment = (ImportSetBundleSongFragment) importBundleViewPagerAdapter.createFragment(1);
            }
            importSetBundleViewPager = myView.importSetBundleViewPager;
            importSetBundleViewPager.setAdapter(importBundleViewPagerAdapter);
            importSetBundleViewPager.setOffscreenPageLimit(1);
            MyTabLayout tabLayout = myView.importSetBundleTabs;
            tabLayout.setTabTextColors(mainActivityInterface.getPalette().textColor, mainActivityInterface.getPalette().textColor);
            //tabLayout.setTabIconTint(ColorStateList.valueOf(mainActivityInterface.getPalette().textColor));
            tabLayout.setTabIconTint(new ColorStateList(
                    new int[][]{new int[0]},
                    new int[]{mainActivityInterface.getPalette().textColor}
            ));
            tabLayout.setSelectedTabIndicatorColor(mainActivityInterface.getPalette().secondary);

            tabLayout.setupWithViewPager(importSetBundleViewPager, (tab, position) -> {
                switch (position) {
                    case 0:
                        tab.setText(set_list);
                        tab.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.list_number, null));
                        break;
                    case 1:
                        tab.setText(included_songs);
                        tab.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.music_note, null));
                        break;
                }
            });
            importSetBundleViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                }
            });
            mainActivityInterface.getOpenSongSetBundle().setImportFragments(
                    importSetBundleSetFragment, importSetBundleSongFragment);

            // Now process the bundle zip file on a new thread
            mainActivityInterface.getThreadPoolExecutor().execute(() -> {
                mainActivityInterface.getOpenSongSetBundle().resetVariables();
                // Backup the current set as we need to temporarily use it
                mainActivityInterface.getOpenSongSetBundle().getBackupOfCurrentSet();

                // Now do what we need to extract the content
                if (mainActivityInterface.getImportFilename().endsWith(".ossb")) {
                    mainActivityInterface.getOpenSongSetBundle().unzipFiles(mainActivityInterface.getImportUri(), "SetBundle", "openSong");

                } else if (mainActivityInterface.getImportFilename().endsWith(".justchords")) {
                    mainActivityInterface.getOpenSongSetBundle().unzipFiles(mainActivityInterface.getImportUri(), "SetBundle", "justChords");
                }
            });
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mainActivityInterface.getOpenSongSetBundle().updateActualCurrentSet();
        mainActivityInterface.getOpenSongSetBundle().resetVariables();
    }
}
