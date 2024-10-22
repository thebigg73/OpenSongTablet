package com.garethevans.church.opensongtablet.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsUtilitiesBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

public class UtilitiesMenuFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private SettingsUtilitiesBinding myView;
    private String beatBuddy_string = "", utilities_string="", aeros_string="",
            deeplink_database_utilities="";
    private ActivityResultLauncher<Intent> selectAudioLauncher;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(utilities_string);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = SettingsUtilitiesBinding.inflate(inflater,container,false);

        // Set up the filechooser launcher
        setupLauncher();

        prepareStrings();

        // Set up listeners
        setupListeners();

        return myView.getRoot();
    }

    private void setupLauncher() {
        // Initialise the launchers
        selectAudioLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                try {
                    Intent data = result.getData();
                    if (data != null) {
                        if (data.getData()!=null) {
                            mainActivityInterface.setImportUri(data.getData());
                            AudioPlayerBottomSheet audioPlayerBottomSheet = new AudioPlayerBottomSheet();
                            audioPlayerBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"audioPlayerBottomSheet");
                        } else {
                            mainActivityInterface.getShowToast().error();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mainActivityInterface.getShowToast().error();
                }
            }
        });
    }
    private void prepareStrings() {
        if (getContext()!=null) {
            utilities_string = getString(R.string.utilities);
            beatBuddy_string = getString(R.string.deeplink_beatbuddy_options);
            aeros_string = getString(R.string.deeplink_aeros);
            deeplink_database_utilities = getString(R.string.deeplink_database_utilities);
        }
    }

    private void setupListeners() {
        myView.soundMeter.setOnClickListener(v -> {
            SoundLevelBottomSheet soundLevelBottomSheet = new SoundLevelBottomSheet();
            soundLevelBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"soundLevelBottomSheet");
        });
        myView.tuner.setOnClickListener(v -> {
            TunerBottomSheet tunerBottomSheet = new TunerBottomSheet();
            tunerBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"tunerBottomSheet");
        });
        myView.beatBuddy.setOnClickListener(v -> mainActivityInterface.navigateToFragment(beatBuddy_string,0));
        myView.aeros.setOnClickListener(v -> mainActivityInterface.navigateToFragment(aeros_string,0));
        myView.databaseOptions.setOnClickListener(v -> mainActivityInterface.navigateToFragment(deeplink_database_utilities,0));
        myView.audioRecorder.setOnClickListener(v -> {
            // Show recorder popup window over the home page
            if (getContext()!=null) {
                mainActivityInterface.setRequireAudioRecorder();
                mainActivityInterface.navHome();
            }
        });
        myView.audioPlayer.setOnClickListener(v -> {
            // Ask the user to select a file
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            ArrayList<String> input = new ArrayList<>();
            input.add("audio/*");
            input.add("audio/3gp");
            input.add("video/3gp");
            input.add("video/mp4)");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, input);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI,
                        mainActivityInterface.getStorageAccess().getUriForItem("Media","",""));
            }
            intent.addFlags(mainActivityInterface.getStorageAccess().getAddReadUriFlags());
            selectAudioLauncher.launch(intent);
        });
    }

}
