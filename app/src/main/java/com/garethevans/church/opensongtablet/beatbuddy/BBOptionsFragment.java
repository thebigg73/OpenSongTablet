package com.garethevans.church.opensongtablet.beatbuddy;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsBeatbuddyOptionsBinding;
import com.garethevans.church.opensongtablet.filemanagement.AreYouSureBottomSheet;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BBOptionsFragment extends Fragment {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "BBOptionsFragment";
    private MainActivityInterface mainActivityInterface;
    private SettingsBeatbuddyOptionsBinding myView;
    private String beat_buddy="", deeplink_beatbuddy_commands="", deeplink_beatbuddy_import="",
            website_beatbuddy="", not_available="", reset_string="", success_string="", error_string="";
    private String webAddress;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(beat_buddy);
        mainActivityInterface.updateToolbarHelp(webAddress);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = SettingsBeatbuddyOptionsBinding.inflate(inflater, container, false);

        prepareStrings();

        webAddress = website_beatbuddy;

        // Check if the database exists as we will likely be using it soon...
        checkDatabase();

        setupListeners();
        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            beat_buddy = getString(R.string.beat_buddy);
            deeplink_beatbuddy_commands = getString(R.string.deeplink_beatbuddy_commands);
            deeplink_beatbuddy_import = getString(R.string.deeplink_beatbuddy_import);
            website_beatbuddy = getString(R.string.website_beatbuddy);
            not_available = getString(R.string.not_available);
            reset_string = getString(R.string.beat_buddy_database_reset);
            success_string = getString(R.string.success);
            error_string = getString(R.string.error);
        }
    }

    private void checkDatabase() {
        // If the database file does not exist, create it
        if (getContext()!=null) {
            try (BBSQLite bbsqLite = new BBSQLite(getContext())) {
                mainActivityInterface.getThreadPoolExecutor().execute(() -> {
                    bbsqLite.checkDefaultDatabase();
                    Log.d(TAG, "myDrums:" + bbsqLite.getMyDrumsCount());
                    Log.d(TAG, "mySongs:" + bbsqLite.getMySongsCount());
                });
            }
        }
    }
    private void setupListeners() {
        myView.songCommands.setOnClickListener((view) -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mainActivityInterface.navigateToFragment(deeplink_beatbuddy_commands,0);
            } else {
                mainActivityInterface.getShowToast().doIt(not_available);
            }
        });
        myView.importCSV.setOnClickListener((view) -> mainActivityInterface.navigateToFragment(deeplink_beatbuddy_import,0));
        myView.beatBuddyAutoLookup.setChecked(mainActivityInterface.getBeatBuddy().getBeatBuddyAutoLookup());
        myView.beatBuddyAutoLookup.setOnCheckedChangeListener((compoundButton, b) -> mainActivityInterface.getBeatBuddy().setBeatBuddyAutoLookup(b));
        myView.resetDatabase.setOnClickListener((view) -> {
            AreYouSureBottomSheet areYouSureBottomSheet = new AreYouSureBottomSheet("resetBeatBuddyDatabase",reset_string,
                    null,"BBOptionsFragment",this, null);
            areYouSureBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"AreYouSure");
        });
        myView.beatBuddyBrowse.setOnClickListener((view) -> {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                BBSQLite bbsqLite = new BBSQLite(getContext());
                    BottomSheetBeatBuddySongs bottomSheetBeatBuddySongs = new BottomSheetBeatBuddySongs(
                            null,bbsqLite);
                    bottomSheetBeatBuddySongs.show(mainActivityInterface.getMyFragmentManager(),
                            "BottomSheetBeatBuddySongs");
            });
        });
        myView.useImported.setChecked(mainActivityInterface.getBeatBuddy().getBeatBuddyUseImported());
        myView.useImported.setOnCheckedChangeListener((compoundButton, b) -> mainActivityInterface.getBeatBuddy().setBeatBuddyUseImported(b));
    }

    public void resetDatabase() {
        if (getContext()!=null) {
            try (BBSQLite bbsqLite = new BBSQLite(getContext())) {
                mainActivityInterface.getThreadPoolExecutor().execute(() -> {
                    bbsqLite.resetDatabase();
                    bbsqLite.checkDefaultDatabase();
                    Log.d(TAG, "myDrums:" + bbsqLite.getMyDrumsCount());
                    Log.d(TAG, "mySongs:" + bbsqLite.getMySongsCount());
                    mainActivityInterface.getMainHandler().post(() -> mainActivityInterface.getShowToast().doIt(success_string));
                });
            } catch (Exception e) {
                mainActivityInterface.getShowToast().doIt(error_string);
            }
        }
    }
}
