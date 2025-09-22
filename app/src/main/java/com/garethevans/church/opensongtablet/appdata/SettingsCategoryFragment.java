package com.garethevans.church.opensongtablet.appdata;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsCategoriesNewBinding;

import java.util.Arrays;
import java.util.List;

public class SettingsCategoryFragment extends Fragment {

    private SettingsCategoriesNewBinding myView;
    @Nullable
    @Override
    public @org.jetbrains.annotations.Nullable View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = SettingsCategoriesNewBinding.inflate(inflater, container, false);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void setupViews() {
        List<SettingsCategory> categories = Arrays.asList(
                new SettingsCategory("Storage & Sync", R.drawable.folder_open, R.id.storage_graph),
                new SettingsCategory("Display & Themes", R.drawable.projector_screen, R.id.display_graph),
                new SettingsCategory("Controls & MIDI", R.drawable.pedal, R.id.control_graph),
                new SettingsCategory("Audio & Pads", R.drawable.audio_player, R.id.pads_graph),
                new SettingsCategory("Songs & Sets", R.drawable.music_note, R.id.set_graph),
                new SettingsCategory("Utilities", R.drawable.settings, R.id.utilities_graph),
                new SettingsCategory("About & Language", R.drawable.information, R.id.about_graph)
        );

        SettingsCategoryAdapter adapter = new SettingsCategoryAdapter(categories, v -> {
            SettingsCategory category = (SettingsCategory) v.getTag();
            NavHostFragment.findNavController(this).navigate(category.getNavDestinationId());
        });

        myView.settingsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        myView.settingsRecycler.setAdapter(adapter);
    }
}
